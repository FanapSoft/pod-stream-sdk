package ir.fanap.podstreamsdkexample.ui.player_activity;

import java.util.List;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public interface PlayerConstract {
    interface View{
        void onFileReady();

        void isLoading(boolean isloading);

        void hasError(String error);

        void onRecivedVideoList(List<VideoItem> response);

    }

    interface Presenter{
        void init();
        void prepare(FileSetup fileSetup);
        void destroy();
        void getVideoList();
    }
}
