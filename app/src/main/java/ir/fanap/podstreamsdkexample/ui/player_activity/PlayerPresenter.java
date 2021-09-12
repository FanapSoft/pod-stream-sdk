package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.app.Activity;
import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamHandler;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class PlayerPresenter implements PlayerConstract.Presenter, StreamHandler.StreamEventListener {
    PodStream offlinestreamer;
    PlayerConstract.View mView;
    Activity mContext;
    Repository repository;

    public PlayerPresenter(Activity context, PlayerConstract.View view) {
        repository = Repository.getInstance();
        mContext = context;
        mView = view;
        offlinestreamer = repository.getOfflinestreamer();
        offlinestreamer.initPlayer(mContext);
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
    public void onStreamerReady(boolean state) {
        mView.onStreamerReady(state);
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        mView.isLoading(isLoading);
    }

    @Override
    public void hasError(String error, int errorCode) {
        mView.hasError(error);
        if (errorCode == 17) {
            mView.timeOutHappend();
            repository.getOfflinestreamer().clean();
            repository.getOfflinestreamer().prepareTopic();
        }
        else if (errorCode == 18) {
            mView.onPlayerError();
            repository.getOfflinestreamer().clean();
            repository.getOfflinestreamer().prepareTopic();
        }
    }

    @Override
    public void destroy() {
        offlinestreamer.releasePlayer();
    }

    @Override
    public void setPlayer() {
        repository.getOfflinestreamer().initPlayer(mContext);
    }
}
