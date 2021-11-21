package ir.fanap.podstream.datasources;

public interface BaseListener {
    //    void onError(String errorMessage);
    default void onError(int code, String message) {
    }
}
