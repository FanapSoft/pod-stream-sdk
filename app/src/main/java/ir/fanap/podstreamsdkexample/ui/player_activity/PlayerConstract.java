package ir.fanap.podstreamsdkexample.ui.player_activity;
import com.google.android.exoplayer2.ui.PlayerView;
import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;

public interface PlayerConstract {
    interface View{
        void onStreamerReady(boolean state);

        void isLoading(boolean isloading);

        void hasError(String error);
        void timeOutHappend();
        void onPlayerError();
        void onReset();


    }

    interface Presenter{
        void init();
        void seekTo(long positionMs);
        void prepare(FileSetup fileSetup,  PlayerView playerView);
        void destroy();
    }
}
