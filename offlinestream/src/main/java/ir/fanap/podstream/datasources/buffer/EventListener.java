package ir.fanap.podstream.datasources.buffer;


import ir.fanap.podstream.datasources.model.VideoPacket;

public abstract class EventListener {
    public interface ProviderListener {

        void providerIsReady(long filSize);

        void write(int[] data);

        void isEnd(boolean isEnd);

        void onStreamerIsReady(boolean state);

        void onError(int code, String message);

        void onStart();
    }

    public interface KafkaListener {

        default void onConnect() {
        }

        default void write(VideoPacket packet) {

        }

        void isEnd(boolean isEnd);

        default void onFileReady(long fileSize) {
        }

        default void onStreamEnd() {
        }

        default void onFileBytes(byte[] bytes, long start, long end) {
        }

        default void onError(int code, String message) {
        }

        default void onDisconnect() {
        }
    }

    public interface BufferListener {
        void fileReady(long filSize);

        void onConnect();

        void onDisconnect();

        void isEnd(boolean isEnd);
    }
}
