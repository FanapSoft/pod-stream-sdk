package ir.fanap.podstream.offlineStream;


import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
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

import java.util.Date;
import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.DataSources.FileDataSource;
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
    //    private FileDataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    private static AppApi api;

    private static HashMap<String, DashResponse> cachManifestUrls;


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
            cachManifestUrls = new HashMap<>();

        }
        return instance;

    }

    private static void initPlayer(Activity activity) {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
//        builder.setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
//        builder.setBufferDurationsMs(
//                1000,
//                30000,
//                500,
//                0
//        );
        builder.setBackBuffer(10000,true);
        player = new SimpleExoPlayer.Builder(activity).setLoadControl(builder.build()).build();
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

    Date start;
    public void prepareStreaming(FileSetup file) {
        start = new Date();
        if (!checkInCacheExist(file))
            mCompositeDisposable.add(api.getDashManifest(file.getUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(response -> {
                                fileReadyToPlay(response);
                                addFileToCache(file, response);
                            },
                            throwable -> ShowLog(LogTypes.ERROR, throwable.getMessage())));

    }

    private void addFileToCache(FileSetup file, DashResponse response) {
        if (!cachManifestUrls.containsKey(file.getUrl())) {
            Log.e(TAG, "add file to cache ");
            cachManifestUrls.put(file.getUrl(), response);
        }
    }

    private boolean checkInCacheExist(FileSetup file) {
        if (cachManifestUrls.containsKey(file.getUrl())) {
            Log.e(TAG, "get file from cache ");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    attachPlayer(cachManifestUrls.get(file.getUrl()));

                }
            }).start();
            return true;
        }

        return false;
    }

    public void fileReadyToPlay(DashResponse response) {

        Log.e("testbuffer", "give response: " + (new Date().getTime() - start.getTime()));
        isReady = true;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                attachPlayer(response);
            }
        });

    }

    private FileDataSource.Factory buildDataSourceFactory() {
        return new FileDataSource.Factory();
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
//        dataSourceFactory = buildDataSourceFactory();
        mediaSource = buildMediaSource();
        if (player != null) {
//            if (player.isPlaying()||player.isLoading()) {
//                Log.e(TAG, "release player: " );
//                player.release();
//            }

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
