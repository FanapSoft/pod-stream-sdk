package ir.fanap.podstreamsdkexample.ui.splash_activity;

public interface SplashConstract {
    interface View{
        void onStreamerReady(boolean state);
    }

    interface Presenter{
        void init(String token);

    }
}
