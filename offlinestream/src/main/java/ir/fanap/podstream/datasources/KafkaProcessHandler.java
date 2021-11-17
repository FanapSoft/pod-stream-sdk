package ir.fanap.podstream.datasources;

public abstract class KafkaProcessHandler {
    public interface ProccessHandler {
        default void onFileReady(long fileSize) { }
        default void onStreamEnd() { }
        default void onFileBytes(byte[] bytes) { }
        default void onError(int code , String message) { }
        default void onConnect() { }
        default void onDisconnect() { }
    }

}
