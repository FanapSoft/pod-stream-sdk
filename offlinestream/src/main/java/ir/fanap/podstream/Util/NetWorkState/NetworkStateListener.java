package ir.fanap.podstream.Util.NetWorkState;

public interface NetworkStateListener {

    void networkAvailable();

    void networkUnavailable();

    default void sendPingToServer(){}

    default void onConnectionLost(){}

}