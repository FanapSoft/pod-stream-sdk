package ir.fanap.podstream.offlinestream;


import ir.fanap.podstream.entity.ErrorOutPut;
import ir.fanap.podstream.network.response.DashResponse;

public interface StreamListener {
    default void onStreamerReady(boolean state) {
    }

    default void onIsLoadingChanged(boolean isLoading) {
    }

    default void onReset() {
    }

    default void onError(String content, ErrorOutPut error) {

    }
}

