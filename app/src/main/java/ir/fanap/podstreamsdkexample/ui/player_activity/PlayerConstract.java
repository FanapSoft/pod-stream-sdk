package ir.fanap.podstreamsdkexample.ui.player_activity;
import com.google.android.exoplayer2.ui.PlayerView;
import ir.fanap.podstream.Entity.FileSetup;

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
        void prepare(FileSetup fileSetup,  PlayerView playerView);
        void destroy();
    }
}
