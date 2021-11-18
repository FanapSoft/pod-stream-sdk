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


    Listener listener;
    KafkaManager kafkaManager;
    private BufferManager bufferManager;
    boolean cancelled = false;
    ByteArrayOutputStream output = null;
    int retryCount = 0;

    public DataProvider(TopicResponse kafkaConfigs, String token, Listener listener) {
        this.listener = listener;
        kafkaManager = new KafkaManager(this);
        bufferManager = new BufferManager(kafkaManager);
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
                bufferManager.startUpdaterJob();
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

    public byte[] getBuffer(long offset, long length) throws Exception {
        output = new ByteArrayOutputStream();
        while (true) {
            if (!bufferManager.existInBuffer(offset, length)) {
                    bufferManager.resetBuffer(offset);
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

        return output.toByteArray();
    }

    public void endStreaming() {
        cancelled = true;
        kafkaManager.reset();
        bufferManager.release();
    }

    public void close() {
        endStreaming();
        kafkaManager.produceCloseMessage();
    }
}
