package ir.fanap.podstream.offlineStream;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.DataSources.ProgressiveDataSource;
import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.R;
import ir.fanap.podstream.Util.LogTypes;
import ir.fanap.podstream.network.AppApi;
import ir.fanap.podstream.network.RetrofitClient;
import ir.fanap.podstream.network.response.DashResponse;

public class PodStream {
    private static CompositeDisposable mCompositeDisposable;
    private static PodStream instance;
    private static Activity mContext;
    private static Gson gson;
    private static StreamEventListener listener;
    private boolean isReady = false;
    private static boolean showLog = true;
    public static String TAG = "PodStream";

    private static StyledPlayerView playerView;
    private static SimpleExoPlayer player;
    private ProgressiveDataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    private static AppApi api;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity) {

        if (instance == null) {
            mContext = activity;
            netWorkInit();
            instance = new PodStream();
            mCompositeDisposable = new CompositeDisposable();
            gson = new GsonBuilder().setPrettyPrinting().create();
            initPlayer(activity);
        }
        return instance;

    }

    private static void initPlayer(Activity activity) {
        player = new SimpleExoPlayer.Builder(activity).build();
        playerView = activity.findViewById(R.id.player_view);
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);
        player.addListener(new ExoPlayer.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                ShowLog(LogTypes.PLAYERSTATE, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                ShowLog(LogTypes.PLAYERSTATE, "onTracksChanged");
            }


            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                ShowLog(LogTypes.PLAYERSTATE, "onIsLoadingChanged");
            }


            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                playerView.hideController();
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    listener.onIsLoadingChanged(true);
                } else {
                    listener.onIsLoadingChanged(false);
                }
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                ShowLog(LogTypes.PLAYERSTATE, "onPlaybackStateChanged" + state);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                //Catch here, but app still crash on some errors!
                ShowLog(LogTypes.PLAYERERROR, "onPlayerError" + error.type);
                ShowLog(LogTypes.PLAYERERROR, "onPlayerError" + error.getMessage());
            }
        });
    }

    private static void netWorkInit() {
        api = RetrofitClient.getInstance().create(AppApi.class);
    }

    public void setListener(StreamEventListener listener) {
        this.listener = listener;
    }

    public void prepareStreaming(FileSetup file) {

        mCompositeDisposable.add(api.getDashManifest(file.getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        this::fileReadyToPlay,
                        throwable -> ShowLog(LogTypes.ERROR, throwable.getMessage())));

    }

    public void fileReadyToPlay(DashResponse response) {

        isReady = true;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attachPlayer(response);
            }
        });

    }

    private ProgressiveDataSource.Factory buildDataSourceFactory(DashResponse response) {
        return new ProgressiveDataSource.Factory(response);
    }

    private MediaSource buildMediaSource() {
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.EMPTY)
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build();

        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);

    }

    public void disableLods() {
        showLog = false;
    }


    public void stopStreaming() {

    }


    public void attachPlayer(DashResponse response) {

        dataSourceFactory = buildDataSourceFactory(response);
        mediaSource = buildMediaSource();
        if (player != null) {
            player.prepare(mediaSource, false, true);
        }


    }


    public void releasePlayer() {
        if (dataSourceFactory != null) {
            isReady = false;
            mCompositeDisposable.dispose();
            dataSourceFactory.getDataSource().release();
        }
    }

    private static void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
        listener.hasError(message);
    }
}
