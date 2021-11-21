package ir.fanap.podstream.datasources.customdatasource;

public abstract class KafkaProcessHandler {
    public interface ProccessHandler {
        default void onFileReady(long fileSize) { }
        default void onStreamEnd() { }
        default void onFileBytes(byte[] bytes , long start,long end) { }
        default void onError(int code , String message) { }
        default void onConnect() { }
        default void onDisconnect() { }
    }

}
