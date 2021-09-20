package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.app.Activity;

import java.util.List;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public interface PlayerConstract {
    interface View{
        void onStreamerReady(boolean state);

        void isLoading(boolean isloading);

        void hasError(String error);
        void timeOutHappend();
        void onPlayerError();



    }

    interface Presenter{
        void init();
        void prepare(FileSetup fileSetup, Activity activity);
        void destroy();
        void setPlayer();
    }
}
