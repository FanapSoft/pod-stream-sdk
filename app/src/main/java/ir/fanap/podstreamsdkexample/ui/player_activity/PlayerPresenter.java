package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.app.Activity;
import android.content.Context;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamEventListener;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class PlayerPresenter implements PlayerConstract.Presenter, StreamEventListener {
    PodStream offlinestreamer;
    PlayerConstract.View mView;
    Context mContext;
    Repository repository;

    public PlayerPresenter(Activity context, PlayerConstract.View view) {
        repository = Repository.getInstance();
        mContext = context;
        mView = view;
        offlinestreamer = PodStream.init(context);
        init();
    }

    @Override
    public void init() {
        offlinestreamer.setListener(this);
    }

    @Override
    public void prepare(FileSetup file) {
        offlinestreamer.prepareStreaming(file);
    }

    @Override
    public void onFileReady() {
        mView.onFileReady();
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        mView.isLoading(isLoading);
    }

    @Override
    public void hasError(String error) {
        mView.hasError(error);
    }

    @Override
    public void destroy() {
        offlinestreamer.clean();
    }

    @Override
    public void getVideoList() {
        mView.onRecivedVideoList(repository.getVideo());
    }

}
