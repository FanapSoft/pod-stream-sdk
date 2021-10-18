package ir.fanap.podstream.offlinestream;

import ir.fanap.podstream.network.response.AvoidObfuscate;
import ir.fanap.podstream.network.response.DashResponse;

public class StreamHandler {

    public interface StreamEventListener extends AvoidObfuscate {
        default void onStreamerReady(boolean state){};
        default void onIsLoadingChanged(boolean isLoading){};
        default void onReset(DashResponse response){};
        void hasError(String error,int errorCode);
    }

}
