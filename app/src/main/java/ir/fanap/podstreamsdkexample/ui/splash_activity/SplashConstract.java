package ir.fanap.podstreamsdkexample.ui.splash_activity;

public interface SplashConstract {
    interface View{
        void onStreamerReady(boolean state);
        void onError(String error);
    }

    interface Presenter{
        void init(String token);

    }
}
