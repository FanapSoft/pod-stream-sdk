package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.app.Activity;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;

import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.offlinestream.StreamHandler;
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
        init();
    }

    @Override
    public void init() {
        offlinestreamer.setListener(this);
    }

    @Override
    public void seekTo(long positionMs) {
        offlinestreamer.getPlayer().seekTo(positionMs);
        offlinestreamer.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                Log.e("Activity", "onTimelineChanged: " + timeline.toString());
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Log.e("Activity", "isPlaying: " + isPlaying);
            }

        });
    }

    @Override
    public void onReset(DashResponse response) {
        mView.onReset();
    }

    @Override
    public void prepare(FileSetup file, PlayerView playerView) {
        offlinestreamer.setContext(mContext);
        offlinestreamer.prepareStreaming(file, playerView);
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
        } else if (errorCode == 18) {
            mView.onPlayerError();
        }
    }

    @Override
    public void destroy() {
        offlinestreamer.releasePlayer();
    }
}
