package ir.fanap.podstreamsdkexample;

import android.app.Activity;
import android.content.Context;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstream.offlineStream.StreamEventListener;

public class MainPresenter implements MainConstract.Presenter, StreamEventListener {
    PodStream offlinestreamer;
    MainConstract.View mView;
    Context mContext;

    public MainPresenter(Activity context, MainConstract.View view) {
        mContext = context;
        mView = view;
        offlinestreamer = PodStream.init(context);
        init();
    }

    @Override
    public void init() {
        offlinestreamer.setListener(this);
        mView.isLoading(true);
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
        offlinestreamer.releasePlayer();
    }

}
