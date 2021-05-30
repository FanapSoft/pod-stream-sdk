package ir.fanap.podstream.offlineStream;

public interface StreamEventListener {

    void onStreamerReady(boolean state);

    void onIsLoadingChanged(boolean isLoading);

    void hasError(String error);

}
