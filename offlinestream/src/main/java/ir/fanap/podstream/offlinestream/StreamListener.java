package ir.fanap.podstream.offlinestream;


import ir.fanap.podstream.model.ErrorOutPut;

public interface StreamListener {
    default void onStreamerReady(boolean state) {
    }

    default void onIsLoadingChanged(boolean isLoading) {
    }

    default void onError(String content, ErrorOutPut error) {

    }
}

