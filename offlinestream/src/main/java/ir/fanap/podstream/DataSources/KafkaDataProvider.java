package ir.fanap.podstream.DataSources;

import android.util.Log;

import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;

import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.network.response.DashResponse;

public class KafkaDataProvider {
    interface KafkaProviderCallBack {

    }

    public KafkaDataProvider(DashResponse dashFile) {
        this.consumTopic = dashFile.getConsumTopic();
        this.produceTopic = dashFile.getProduceTopic();
        this.filmLength = dashFile.getSize();
        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", dashFile.getBrokerAddress());
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", "777");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();
        consumerClient.consumingTopic(5);
        getStartOfFile();
        getEndOfFile();
       // updateBuffer(0, Constants.DefualtLengthValue);
    }

    KafkaProviderCallBack listener;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;


    private byte[] mainBuffer;
    private byte[] startBuffer;
    private byte[] endBuffer;
    private long offsetMainBuffer;
    private long endOfMainBuffer;
    private long filmLength;

    public void setListener(KafkaProviderCallBack listener) {
        this.listener = listener;
    }

    public void getStartOfFile() {
        Thread taskforstart = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
                buffers.putLong(-3);
                producerClient.produceMessege(buffers.array(), 0 + "," + 250000, produceTopic);
                startBuffer = consumerClient.consumingTopic(5);
                while (startBuffer == null || startBuffer.length < 250000) {
                    startBuffer = consumerClient.consumingTopic(5);
                }
                Log.e("Buffering", "start is done" + startBuffer.length);
            }
        });
        taskforstart.start();
    }

    public void getEndOfFile() {
        Thread taskforend = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
                buffers.putLong(-3);
                producerClient.produceMessege(buffers.array(), filmLength - 250000 + "," + filmLength, produceTopic);
                endBuffer = consumerClient.consumingTopic(5);
                while (endBuffer == null || endBuffer.length < 250000) {
                    endBuffer = consumerClient.consumingTopic(5);
                }
                Log.e("Buffering", "end is done" + endBuffer.length);
            }
        });
        taskforend.start();
    }

    public byte[] getDataBuffer() {
        return mainBuffer;
    }

    public long getOffsetMainBuffer() {
        return offsetMainBuffer;
    }

    public byte[] getStartBuffer() {
        return startBuffer;
    }

    public byte[] getEndBuffer() {
        return endBuffer;
    }

    public boolean isExistInEndBuffer(long offset, long Length) {
        if (startBuffer == null)
            return false;

        return false;
    }

    public boolean isExistInStartBuffer(long offset, long length,long readPosition) {
        if (endBuffer == null || readPosition >250000)
            return false;

        return offset > 0 && offset <= 250000 && (offset + length) <= 250000;
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
            System.out.println("offset :" + i + " length : " + newlength);
            if (exit)
                break;
        }
    }


    public boolean shouldUpdateBuffer(long offset, long length) {
        if (offset < endOfMainBuffer && offset >= offsetMainBuffer && (offset + length) <= endOfMainBuffer)
            return false;
        return true;
    }

    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);
    }

}
