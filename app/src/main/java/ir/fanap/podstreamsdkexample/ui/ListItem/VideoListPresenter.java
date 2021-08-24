package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamEventListener;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class VideoListPresenter implements VideoListConstract.Presenter, StreamEventListener {

    VideoListConstract.View mView;
    Activity mContext;
    Repository repository;
    String token;

    public VideoListPresenter(Activity context, VideoListConstract.View view) {
        mContext = context;
        mView = view;
        repository = Repository.getInstance();

    }

    int a = 0;

    @Override
    public void init(String token) {
        repository.Streamer(mContext, token);
        if (a == 0) {
            setlistener();
            a = 5;
        }
    }

    public void setlistener() {
        repository.getOfflinestreamer().setListener(this);
    }

    @Override
    public void destroy() {
        repository.getOfflinestreamer().clean();
    }

    @Override
    public void getVideoList() {
        mView.onRecivedVideoList(repository.getVideo());
    }

    @Override
    public void onStreamerReady(boolean state) {
        mView.onStreamerReady(state);
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        Log.d("test", "hasError: ");
    }

    @Override
    public void hasError(String error) {
        Log.d("test", "hasError: ");
    }
}
