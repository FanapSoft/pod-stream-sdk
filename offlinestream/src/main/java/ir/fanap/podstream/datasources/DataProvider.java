package ir.fanap.podstream.datasources;

import android.util.Log;

import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.Utils;

public class DataProvider {

    public interface Listener {
        void onStreamerIsReady(boolean state);

        void onFileReady();

        void onTimeOut();

        void onError(int code, String message);
    }

    private final long KAFKA_MEESSAGE_GET_FILE_INFORMATION = -5;
    private final long KAFKA_MEESSAGE_GET_FILE_BYTE = -3;
    private final long KAFKA_MEESSAGE_STOP_STREAMING = -2;

    String token;
    Listener listener;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    Thread consumerThread, bufferManager;
    int startBufferedIndex = 0;
    long endBufferedIndex = Constants.DefaultLengthValue;
    long fileSize = 0;

    boolean isBusy = false;
    boolean isEndOfStream = false;
    boolean endOfTasks = false;
    Stack<VideoPacket> packetbuffer = new Stack<>();


    public DataProvider(TopicResponse kafkaConfigs, String token, Listener listener) {
        this.listener = listener;
        this.token = token;
        consumTopic = kafkaConfigs.getStreamTopic();
        produceTopic = kafkaConfigs.getControlTopic();
        final Properties properties = Utils.getSslProperties(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath());
        connectProducer(properties);
        connectConsumer(properties);
        if (listener != null)
            listener.onStreamerIsReady(true);
    }

    private void connectProducer(Properties properties) {
        producerClient = new ProducerClient(properties);
        producerClient.connect();
    }

    public Listener getListener() {
        return listener;
    }

    private void connectConsumer(Properties properties) {
        properties.setProperty("group.id", "264");
        properties.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(properties, consumTopic);
        consumerClient.connect();
    }

    private void startBufferManagerTask() {
        endOfTasks = false;
        bufferManager = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!endOfTasks && !isEndOfStream) {
                    try {
                        Thread.sleep(1000);
                        int bufferSize = packetbuffer.size();
                        long packetLength = Constants.DefaultLengthValue;
                        if (bufferSize < 20) {
                            if (startBufferedIndex + packetLength > fileSize) {
                                packetLength = fileSize - startBufferedIndex;
                                isEndOfStream = true;
                                Log.e("buffermanager", "end of file: " + bufferSize);
                            }
                            produceMessage(KAFKA_MEESSAGE_GET_FILE_BYTE, startBufferedIndex + "," + packetLength);
                            Log.e("buffermanager", "send packet message: " + packetLength);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        consumerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String key = "";
                while (!endOfTasks) {
                    ConsumResult cr = consumerClient.consumingWithKey(100);
                    key = new String(cr.getKey());
                    byte[] data = cr.getValue();
                    if (!key.equals("")) {
                        switch (key) {
                            case "5":
                                if (!isBusy) {
                                    long size = Utils.byteArrayToLong(data);
                                    fileSize = size;
                                    isBusy = true;
                                    bufferManager.start();
                                    listener.onFileReady();
                                    Log.e("buffermanager", "fileSize :" + fileSize);
                                }
                                break;
                            default:
                                Log.e("buffermanager", "recived packet :" + startBufferedIndex + " --- " + endBufferedIndex);
                                startBufferedIndex = startBufferedIndex + Constants.DefaultLengthValue;
                                endBufferedIndex = endBufferedIndex + Constants.DefaultLengthValue;

                                if (startBufferedIndex + Constants.DefaultLengthValue > fileSize)
                                    endBufferedIndex = fileSize;
                                VideoPacket packet = new VideoPacket(data, startBufferedIndex, endBufferedIndex);
                                packetbuffer.push(packet);
                                break;
                        }
                    }
                }
            }
        });
        consumerThread.start();
    }

    public void endBufferManagerTask() {
        packetbuffer.clear();
        endOfTasks = true;
        isEndOfStream = true;
        isBusy = false;
    }

    private long offsetMainBuffer;
    private long endOfMainBuffer;

    public void startStreming(FileSetup file) {
        startBufferManagerTask();
        try {
            Thread.sleep(1000);
            produceMessage(KAFKA_MEESSAGE_GET_FILE_INFORMATION, file.getVideoAddress() + "," + token);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBuffer(long offset, int length) {
        while (!checkExistInBuffer(offset, length)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        output = new ByteArrayOutputStream();
        pickFromBuffer(offset, length);
        return output.toByteArray();
    }

    ByteArrayOutputStream output = null;

    private boolean checkExistInBuffer(long offset, int length) {
        if (endBufferedIndex < (offset + length) || packetbuffer.size() == 0)
            return false;
        return true;
    }

    public long getOffsetMainBuffer() {
        return offsetMainBuffer;
    }

    public DataProvider setOffsetMainBuffer(long offsetMainBuffer) {
        this.offsetMainBuffer = offsetMainBuffer;
        return this;
    }

    private void pickFromBuffer(long offset, long length) {
        VideoPacket packet = packetbuffer.pop();
        long packetOffset = 5;
        long packetLenth = 15;
        try {
            output.write(packet.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        offsetMainBuffer = offset;
        endOfMainBuffer = offset + (length - 1);
        if ((packetOffset + packetLenth) < (offset + length)) {
            pickFromBuffer(packet.getLength(), packet.getLength());
        }
    }

    public static byte[] addAll(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public void produceMessage(long message, String key) {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(message);
        producerClient.produceMessege(buffers.array(), key, produceTopic);
    }
}
