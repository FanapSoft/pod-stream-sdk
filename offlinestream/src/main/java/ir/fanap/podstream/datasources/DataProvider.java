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

import ir.fanap.podstream.datasources.buffer.BufferManager;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.Utils;

public class DataProvider implements KafkaProcessHandler.ProccessHandler {
    public interface Listener {
        void onStreamerIsReady(boolean state);

        void onError(int code, String message);

        void onStart();
    }

    String token;
    Listener listener;
    KafkaManager kafkaManager;
    private BufferManager bufferManager;
    Thread producerThread;
    boolean streamIsStarted = false;
    boolean cancelled = false;
    ByteArrayOutputStream output = null;
    boolean isWaitingForPacket = false;
    boolean isWaitingForUpdateBuffer = false;
    int retryCount = 0;

    private long offsetBuffer;
    private long endOfBuffer;

    public DataProvider(TopicResponse kafkaConfigs, String token, Listener listener) {
        this.listener = listener;
        this.token = token;
        kafkaManager = new KafkaManager(this);
        bufferManager = new BufferManager();
        kafkaManager.connect(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath(), kafkaConfigs.getStreamTopic(), kafkaConfigs.getControlTopic(), token);
    }

    @Override
    public void onConnect() {
        listener.onStreamerIsReady(true);
    }

    @Override
    public void onDisconnect() {
        listener.onStreamerIsReady(false);
    }

    public long getFileSize() {
        return kafkaManager.fileSize;
    }

    public Listener getListener() {
        return listener;
    }


    public void startStreming(FileSetup file) {
        kafkaManager.produceFileSizeMessage(file.getVideoAddress(), new KafkaProcessHandler.ProccessHandler() {
            @Override
            public void onFileReady(long fileSize) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
                streamIsStarted = true;
                startUpdaterJob();
                listener.onStart();
            }

            @Override
            public void onError(int code, String message) {
                if (retryCount < 4 && !cancelled) {
                    Log.e("TAG", "onError:  retry" + retryCount);
                    retryCount++;
                    startStreming(file);
                } else {
                    listener.onError(0, "time out error");
                    retryCount = 0;
                }
            }
        });
    }

    private void startUpdaterJob() {
        producerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO update buffer in here
                while (streamIsStarted) {
                    try {
                        if (bufferManager.needsUpdate() && !isWaitingForPacket) {
                            isWaitingForPacket = true;
                            getNextChank();
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        producerThread.start();
    }

    private void getNextChank() {
        kafkaManager.produceNextChankMessage(new KafkaProcessHandler.ProccessHandler() {
            @Override
            public void onFileBytes(byte[] bytes, long start, long end) {

                VideoPacket packet = new VideoPacket(bytes, start, end);
                bufferManager.addToBuffer(packet);
                isWaitingForPacket = false;
                if (isWaitingForUpdateBuffer && bufferManager.checkEnoughBuffered())
                    isWaitingForUpdateBuffer = false;
            }

            @Override
            public void onStreamEnd() {
                streamIsStarted = false;
                isWaitingForPacket = true;
            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }


    public long getOffsetMainBuffer() {
        return endOfBuffer;
    }

    int count = 0;

    public byte[] getBuffer(long offset, long length) throws Exception {
        if (count < 100) {
            Log.e("buffer", "offset: " + offset + "length : " + length);
            count++;
        }
        output = new ByteArrayOutputStream();
        while (true) {
            if (!bufferManager.existInBuffer(offset, length)) {
                if (!isWaitingForUpdateBuffer) {
                    isWaitingForUpdateBuffer = true;
                    bufferManager.resetBuffer(offset, length);
                    kafkaManager.changeStartOffset(offset);
                    Thread.sleep(3000);
                }
                continue;
            }

            if (!bufferManager.existInCurrent(offset, length)) {
                bufferManager.changeCurrentPacket();
                continue;
            }

            if (bufferManager.existInCurrent(offset, length)) {
                output.write(bufferManager.getCurrentPacket().getBytes());
                break;
            }

            if (bufferManager.existInCurrentAndNext(offset, length)) {
                output.write(bufferManager.getCurrentPacket().getBytes());
                offset = bufferManager.getNextOffset();
                bufferManager.changeCurrentPacket();
                continue;
            }
        }
        offsetBuffer = offset;
        endOfBuffer = offset + (length - 1);
        return output.toByteArray();
    }

    public void endStreaming() {
        streamIsStarted = false;
        isWaitingForPacket = false;
        cancelled = true;
        kafkaManager.reset();
    }

    public void close() {
        endStreaming();
        kafkaManager.produceCloseMessage();
    }
}
