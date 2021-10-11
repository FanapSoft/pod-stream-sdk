package ir.fanap.podstreamsdkexample.ui.splash_activity;

import android.app.Activity;

import com.google.android.exoplayer2.ui.PlayerView;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamHandler;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class SplashPresenter implements SplashConstract.Presenter, StreamHandler.StreamEventListener {
    PodStream offlinestreamer;
    SplashConstract.View mView;
    Activity mContext;
    Repository repository;

    public SplashPresenter(Activity context, SplashConstract.View view) {
        repository = Repository.getInstance();
        mContext = context;
        mView = view;
        offlinestreamer = repository.getOfflinestreamer();

    }


    @Override
    public void init(String token) {
        repository.Streamer(mContext, token);
        repository.getOfflinestreamer().setToken(token);
        repository.getOfflinestreamer().setListener(this);
    }

    @Override
    public void onStreamerReady(boolean state) {
        repository.getOfflinestreamer().setListener(this);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mView.onStreamerReady(true);
    }

    @Override
    public void hasError(String error, int errorCode) {

    }
}
