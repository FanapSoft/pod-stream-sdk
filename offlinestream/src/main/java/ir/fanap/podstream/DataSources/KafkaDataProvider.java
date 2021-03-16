package ir.fanap.podstream.DataSources;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Assertions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Properties;

import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

public class KafkaDataProvider {
    Listener listener;

    public  interface Listener{
       void onStreamerIsReady(boolean state);
    }

    public KafkaDataProvider(DashResponse dashFile) {
        isEndBufferFill = false;
        this.consumTopic = dashFile.getConsumTopic();
        this.produceTopic = dashFile.getProduceTopic();
        this.filmLength = dashFile.getSize();
        if (filmLength ==0)
            filmLength = 10000;
        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", dashFile.getBrokerAddress());
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", "264");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();

     //   getEndOfFile();

    }

    public void startStreming(DashResponse dashFile){
        this.filmLength = dashFile.getSize();
        getStartOfFile();
    }

    public KafkaDataProvider(TopicResponse kafkaConfigs,Listener listener) {
        this.listener = listener;
        isEndBufferFill = false;
        consumTopic = kafkaConfigs.getStreamTopic();
        produceTopic = kafkaConfigs.getControlTopic();

        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", kafkaConfigs.getBrokerAddress());
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", "264");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        Date start = new Date();
        consumerClient.connect();
        Log.e("testbuffer", "give start of file: " + (new Date().getTime() - start.getTime()));
        if(listener!=null)
            listener.onStreamerIsReady(true);

    }

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

    private boolean isEndBufferFill = false;

    public void getStartOfFile() {
        Date start =new Date();
        while (startBuffer == null || startBuffer.length < 250000) {
            startBuffer = consumerClient.consumingTopic(1000);
        }
        Log.e("testbuffer", "give start buffer: " + (new Date().getTime()-start.getTime()));

//        Thread taskforstart = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
//                buffers.putLong(-3);
//                producerClient.produceMessege(buffers.array(), 0 + "," + 250000, produceTopic);
//                startBuffer = consumerClient.consumingTopic(5);
//                while (startBuffer == null || startBuffer.length < 250000) {
//                    startBuffer = consumerClient.consumingTopic(5);
//                }
//                Log.e("buffering", "start is done" + startBuffer.length);
//            }
//        });
//        taskforstart.start();
    }

    public void getEndOfFile() {
        Thread taskforend = new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
                buffers.putLong(-3);
                producerClient.produceMessege(buffers.array(), filmLength - 250000 + "," + filmLength, produceTopic);
                endBuffer = consumerClient.consumingTopic(1000);
                while (endBuffer == null || endBuffer.length < 250000) {
                    endBuffer = consumerClient.consumingTopic(5);
                }
                Log.e("Buffering", "end is done" + endBuffer.length);
                isEndBufferFill = true;
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

    private void getEndBufferStartOffset(int offset, int length) {
        endBufferStartIndex = (int) this.filmLength - (offset + length);
        endBufferEndIndex = endBuffer.length - endBufferStartIndex;
        endBufferStartIndex = endBufferEndIndex - length;

    }

    int endBufferStartIndex = 0;
    int endBufferEndIndex = 0;

    public int getEndBufferStartOffset() {
        Log.e("testtete", "getEndBufferStartOffset: ");
        return endBufferStartIndex;
    }

    public boolean isExistInEndBuffer(long offset, long Length) {
        if (startBuffer == null)
            return false;
        if (offset > this.filmLength - 250000 && isEndBufferFill) {
            getEndBufferStartOffset((int) offset, (int) Length);
            return true;
        }
        return false;
    }

    public boolean shouldUpdateBuffer(long offset, long length) {
        if (offset < endOfMainBuffer && offset >= offsetMainBuffer && (offset + length) <= endOfMainBuffer)
            return false;

        return true;
    }

    public boolean isExistInStartBuffer(long offset, long length) {
        if ( offset >= 0 && offset < 250000 && (offset + length) <= 250000)
            return true;
        return false;
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

    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);

    }


    private static RandomAccessFile openLocalFile(Uri uri) throws com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException {
        try {
            return new RandomAccessFile(Assertions.checkNotNull(uri.getPath()), "r");
        } catch (FileNotFoundException e) {
            if (!TextUtils.isEmpty(uri.getQuery()) || !TextUtils.isEmpty(uri.getFragment())) {
                throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(
                        String.format(
                                "uri has query and/or fragment, which are not supported. Did you call Uri.parse()"
                                        + " on a string containing '?' or '#'? Use Uri.fromFile(new File(path)) to"
                                        + " avoid this. path=%s,query=%s,fragment=%s",
                                uri.getPath(), uri.getQuery(), uri.getFragment()),
                        e);
            }
            throw new com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException(e);
        }
    }
}