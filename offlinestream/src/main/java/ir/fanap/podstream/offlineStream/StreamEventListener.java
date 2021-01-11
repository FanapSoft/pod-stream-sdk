package ir.fanap.podstream.offlineStream;

public interface StreamEventListener {

    void onFileReady();

    void onIsLoadingChanged(boolean isLoading);

    void hasError(String error);

}
