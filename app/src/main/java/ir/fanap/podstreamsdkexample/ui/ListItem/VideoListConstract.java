package ir.fanap.podstreamsdkexample.ui.ListItem;

import java.util.List;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public interface VideoListConstract {
    interface View{
        void onRecivedVideoList(List<VideoItem> response);
    }

    interface Presenter{
        void init();
        void destroy();
        void getVideoList();

    }
}
