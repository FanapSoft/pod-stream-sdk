package ir.fanap.podstreamsdkexample.ui.splash_activity;

import android.app.Activity;

import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.offlinestream.PodStreamAdapter;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class SplashPresenter extends PodStreamAdapter implements SplashConstract.Presenter {
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
        repository.Streamer(mContext);
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

}
