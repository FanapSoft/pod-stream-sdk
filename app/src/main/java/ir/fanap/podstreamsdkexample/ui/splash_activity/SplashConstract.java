package ir.fanap.podstreamsdkexample.ui.splash_activity;

import com.google.android.exoplayer2.ui.PlayerView;

import ir.fanap.podstream.Entity.FileSetup;

public interface SplashConstract {
    interface View{
        void onStreamerReady(boolean state);
    }

    interface Presenter{
        void init(String token);
    }
}
