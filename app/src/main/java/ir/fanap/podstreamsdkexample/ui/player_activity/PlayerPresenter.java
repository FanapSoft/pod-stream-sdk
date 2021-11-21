package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.app.Activity;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;

import ir.fanap.podstream.model.ErrorOutPut;
import ir.fanap.podstream.model.FileSetup;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.offlinestream.PodStreamAdapter;
import ir.fanap.podstreamsdkexample.data.remote.Repository;

public class PlayerPresenter extends PodStreamAdapter implements PlayerConstract.Presenter {
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
        mView.onRecivedVideoList(repository.getVideo());
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
    public void prepare(FileSetup file) {
        offlinestreamer.prepareStreaming(file);
    }

    @Override
    public void setPLayerView(PlayerView playerView) {
//        offlinestreamer.setContext(mContext);
        offlinestreamer.setPlayerView(playerView);
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
    public void onError(String content, ErrorOutPut error) {
        super.onError(content, error);
        mView.hasError(error.getErrorMessage());
        if (error.getErrorCode() == 17) {
            mView.timeOutHappend();
        } else if (error.getErrorCode() == 18) {
            mView.onPlayerError();
        }
    }

    @Override
    public void destroy() {
        offlinestreamer.stopStreaming();
        offlinestreamer.releasePlayerResource();
    }
}
