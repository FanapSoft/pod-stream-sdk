package ir.fanap.podstreamsdkexample.ui.splash_activity;

import android.app.Activity;

import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.offlinestream.StreamHandler;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class SplashPresenter implements SplashConstract.Presenter, StreamHandler.StreamEventListener {
    SplashConstract.View mView;
    Activity mContext;
    Repository repository;

    public SplashPresenter(Activity context, SplashConstract.View view) {
        repository = Repository.getInstance();
        mContext = context;
        mView = view;
    }


    @Override
    public void init(String token) {
        repository.Streamer(mContext);
        repository.getOfflinestreamer().setBackBufferSize(10000);
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
