package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.app.Activity;
import android.content.Context;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamEventListener;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class VideoListPresenter implements VideoListConstract.Presenter {

    VideoListConstract.View mView;
    Activity mContext;
    Repository repository;

    public VideoListPresenter(Activity context, VideoListConstract.View view) {
        repository = Repository.getInstance();
        repository.Streamer(context);
        mContext = context;
        mView = view;
        init();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        repository.getOfflinestreamer().clean();
    }

    @Override
    public void getVideoList() {
        mView.onRecivedVideoList(repository.getVideo());
    }

}
