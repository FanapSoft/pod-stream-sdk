package ir.fanap.podstreamsdkexample.ui.player_activity;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

import ir.fanap.podstream.model.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public interface PlayerConstract {
    interface View{
        void onStreamerReady(boolean state);

        void isLoading(boolean isloading);

        void hasError(String error);
        void timeOutHappend();
        void onPlayerError();
        void onRecivedVideoList(List<VideoItem> response);

    }

    interface Presenter{
        void init();
        void seekTo(long positionMs);
        void prepare(FileSetup fileSetup);
        void setPLayerView( PlayerView playerView);
        void destroy();
    }
}
