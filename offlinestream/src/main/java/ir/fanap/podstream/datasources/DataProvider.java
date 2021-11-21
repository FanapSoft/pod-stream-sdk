package ir.fanap.podstream.datasources;

import ir.fanap.podstream.datasources.buffer.BufferManager;
import ir.fanap.podstream.datasources.buffer.EventListener;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.TopicResponse;

public class DataProvider implements EventListener.BufferListener {
    EventListener.ProviderListener listener;
    BufferManager bufferManager;
    long fileSize;
    KafkaManager kafkaManager;
    boolean cancelled = false;

    public EventListener.ProviderListener getListener() {
        return listener;
    }

    public DataProvider setListener(EventListener.ProviderListener listener) {
        this.listener = listener;
        return this;
    }

    public DataProvider(TopicResponse kafkaConfigs, String token, EventListener.ProviderListener listener) {
        this.listener = listener;
        kafkaManager = new KafkaManager();
        bufferManager = new BufferManager(kafkaManager).setListener(this);
        kafkaManager.connect(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath(), kafkaConfigs.getStreamTopic(), kafkaConfigs.getControlTopic(), token);
    }

    public void startStreaming(FileSetup file) {

        kafkaManager.produceFileSizeMessage(file.getVideoAddress(), new EventListener.KafkaListener() {

            @Override
            public void write(VideoPacket packet) {

            }

            @Override
            public void isEnd(boolean isEnd) {

            }

            @Override
            public void onFileReady(long fileSize) {
                if (cancelled) {
                    cancelled = false;
                    return;
                }
//                bufferManager.startUpdaterJob();
                listener.onStart();
            }

            @Override
            public void onError(int code, String message) {
//                if (retryCount < 4 && !cancelled) {
//                    Log.e("TAG", "onError:  retry" + retryCount);
//                    retryCount++;
//                    startStreming(file);
//                } else {
//                    listener.onError(0, "time out error");
//                    retryCount = 0;
//                }
            }
        });
    }

    @Override
    public void fileReady(long fileSize) {
        this.fileSize = fileSize;
        listener.providerIsReady(fileSize);
    }

    byte[] read(long offset, long length) {
        return bufferManager.getData(offset, length);
    }


    @Override
    public void isEnd(boolean isEnd) {
        listener.isEnd(isEnd);
    }

    @Override
    public void onConnect() {
        listener.onStreamerIsReady(true);
    }

    @Override
    public void onDisconnect() {
        listener.onStreamerIsReady(false);
    }

    public void endStreaming() {
        cancelled = true;
        bufferManager.release();
    }

    public void close() {
        endStreaming();
        kafkaManager.produceCloseMessage();
    }
}
