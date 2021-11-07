package ir.fanap.podstreamsdkexample.ui.ListItem;

import java.util.List;

import ir.fanap.podstreamsdkexample.data.VideoItem;

public interface VideoListConstract {
    interface View{
        void onRecivedVideoList(List<VideoItem> response);
        void onStreamerReady(boolean state);
    }

    interface Presenter{
        void init(String token);
        void destroy();
        void getVideoList();
    }
}
