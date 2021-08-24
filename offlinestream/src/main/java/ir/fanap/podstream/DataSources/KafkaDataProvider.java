package ir.fanap.podstream.DataSources;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;
import com.google.android.exoplayer2.util.Assertions;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.Util.Utils;

import ir.fanap.podstream.network.response.AvoidObfuscate;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

public class KafkaDataProvider {
    public interface Listener {
        void onStreamerIsReady(boolean state);
        void onFileReady(DashResponse dashFile);
    }

    Listener listener;
    DashResponse dashFile;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    private byte[] mainBuffer;
    private byte[] startBuffer;
    private long offsetMainBuffer;
    private long endOfMainBuffer;
    private long filmLength;


    public KafkaDataProvider(TopicResponse kafkaConfigs, Listener listener) {
        this.listener = listener;
        consumTopic = kafkaConfigs.getStreamTopic();
        produceTopic = kafkaConfigs.getControlTopic();

        final Properties propertiesProducer = Utils.getSslProperties(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath());

        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();

        propertiesProducer.setProperty("group.id", "264");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();
        if (listener != null)
            listener.onStreamerIsReady(true);

    }

    public void prepareDashFileForPlay(String Hash, String Token) {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-5);
        producerClient.produceMessege(buffers.array(), Hash + "," + Token, produceTopic);
        String key = "-1";
        while (!key.startsWith("5")) {
            ConsumResult cr = consumerClient.consumingWithKey(1000);
            key = new String(cr.getKey());
            long fileSize = Utils.byteArrayToLong(cr.getValue());
            this.dashFile.setSize(fileSize);
        }
        if (listener != null) {
            listener.onFileReady(this.dashFile);
        }
    }

    public void startStreming(DashResponse dashFile) {
        this.dashFile = dashFile;
        this.filmLength = dashFile.getSize();
        startBuffer = null;
        while (startBuffer == null || startBuffer.length < 250000) {
            startBuffer = this.consumerClient.consumingWithKey(100).getValue();
        }
    }



    public byte[] getDataBuffer() {
        return mainBuffer;
    }

    public long getOffsetMainBuffer() {
        return offsetMainBuffer;
    }

    public boolean shouldUpdateBuffer(long offset, long length) {
        if (offset < endOfMainBuffer && offset >= offsetMainBuffer && (offset + length) <= endOfMainBuffer)
            return false;
        return true;
    }

    public void updateBuffer(long offset, long length) {
        if (length > Constants.DefualtLengthValue) {
            getData(offset, length);
        } else {
            if ((offset + length) > filmLength)
                length = filmLength - offset;
            offsetMainBuffer = offset;
            endOfMainBuffer = offset + (length - 1);
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), offset + "," + length, produceTopic);
            mainBuffer = consumerClient.consumingTopic(5);
            while (mainBuffer == null || mainBuffer.length < length) {
                mainBuffer = consumerClient.consumingTopic(5);
            }
        }
    }

    public void getData(long offset, long length) {
        mainBuffer = new byte[(int) length];
        offsetMainBuffer = offset;
        endOfMainBuffer = offset + (length - 1);
        boolean exit = false;
        for (int i = 0; i < length; i += Constants.DefualtLengthValue) {
            int newlength = Constants.DefualtLengthValue;
            if (i + newlength > length) {
                newlength = (int) length - i;
                exit = true;
            }
            byte[] newData;
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), (i + offset) + "," + newlength, produceTopic);
            newData = consumerClient.consumingTopic(5);
            while (newData == null || newData.length < newlength) {
                newData = consumerClient.consumingTopic(5);
            }
            System.arraycopy(newData, 0, mainBuffer, i, newlength);
            if (exit)
                break;
        }
    }

    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);
    }

}
