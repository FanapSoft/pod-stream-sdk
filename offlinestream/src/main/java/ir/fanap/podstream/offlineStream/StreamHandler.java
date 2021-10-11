package ir.fanap.podstream.offlineStream;

import ir.fanap.podstream.network.response.AvoidObfuscate;

public class StreamHandler {

    public interface StreamEventListener extends AvoidObfuscate {
        default void onStreamerReady(boolean state){};
        default void onIsLoadingChanged(boolean isLoading){};
        void hasError(String error,int errorCode);
    }

}
