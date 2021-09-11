package ir.fanap.podstream.offlineStream;

import ir.fanap.podstream.network.response.AvoidObfuscate;

public class StreamHandler {

    public interface StreamEventListener extends AvoidObfuscate {
        void onStreamerReady(boolean state);
        void onIsLoadingChanged(boolean isLoading);
        void hasError(String error,int errorCode);
    }

}
