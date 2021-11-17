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
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.Utils;

public class DataProvider implements KafkaProcessHandler.ProccessHandler {
    public interface Listener {
        void onStreamerIsReady(boolean state);

        void onError(int code, String message);
    }

    String token;
    Listener listener;
    KafkaManager kafkaManager;
    Thread producerThread;
    boolean streamIsStarted = false;
    ByteArrayOutputStream output = null;
    boolean isWaitingForPacket = false;
    boolean isWaitingForUpdateBuffer = false;
    int retryCount = 0;

    public DataProvider(TopicResponse kafkaConfigs, String token, Listener listener) {
        this.listener = listener;
        this.token = token;
        kafkaManager = new KafkaManager(this);
        kafkaManager.connect(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath(), kafkaConfigs.getControlTopic(), kafkaConfigs.getStreamTopic(), token);
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
                streamIsStarted = true;
                startUpdaterJob();
            }

            @Override
            public void onError(int code, String message) {
                if (retryCount < 4) {
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
                        Thread.sleep(3000);
                        Log.e("TAG", "run: ");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        producerThread.start();
    }

    public void endStreaming() {
        streamIsStarted = false;
        kafkaManager.produceCloseMessage();
        isWaitingForPacket = false;
    }
}
