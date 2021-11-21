package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;

import ir.fanap.podstream.model.ErrorOutPut;
import ir.fanap.podstream.offlinestream.PodStreamAdapter;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class VideoListPresenter extends PodStreamAdapter implements VideoListConstract.Presenter {

    VideoListConstract.View mView;
    Activity mContext;
    Repository repository;

    public VideoListPresenter(Activity context, VideoListConstract.View view) {
        mContext = context;
        mView = view;
        repository = Repository.getInstance();

    }

    int a = 0;

    @Override
    public void init(String token) {
        repository.Streamer(mContext);
        if (a == 0) {
            setlistener();
            a = 5;
        }
    }

    public void setlistener() {
        repository.getOfflinestreamer().setListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
    public void onError(String content, ErrorOutPut error) {
        super.onError(content, error);
        Log.d("test", "hasError: ");
    }

}
