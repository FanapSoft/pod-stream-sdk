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

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.DataSources.FileDataSource;
import ir.fanap.podstream.DataSources.KafkaDataProvider;
import ir.fanap.podstream.DataSources.ProgressiveDataSource;
import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.R;
import ir.fanap.podstream.Util.LogTypes;
import ir.fanap.podstream.network.AppApi;
import ir.fanap.podstream.network.RetrofitClient;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

public class PodStream implements KafkaDataProvider.Listener {
    public static String TAG = "PodStream";
    private  CompositeDisposable mCompositeDisposable;
    private static PodStream instance;
    private Activity mContext;
    private Gson gson;
    private StreamEventListener listener;
    private boolean showLog = true;
    private StyledPlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;


    private boolean isReady = false;
    private ProgressiveDataSource.Factory dataSourceFactory;
    //    private FileDataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    private KafkaDataProvider provider;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity) {

        if (instance == null) {
            instance = new PodStream();
            instance.setContext(activity);
            instance.netWorkInit();
            instance.initPlayer(activity);
            instance.prepareTopic();
        }
        return instance;

    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
    }

    private void initPlayer(Activity activity) {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
//        builder.setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
//        builder.setBufferDurationsMs(
//                1000,
//                30000,
//                500,
//                0
//        );
        builder.setBackBuffer(10000, true);
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

    private void netWorkInit() {
        api = RetrofitClient.getInstance().create(AppApi.class);
        gson = new GsonBuilder().setPrettyPrinting().create();
        mCompositeDisposable = new CompositeDisposable();
    }

    private void prepareTopic() {
        mCompositeDisposable.add(api.getTopics("http://192.168.112.32/getTopic/?clientId=7936886af8064418b01e97f57c377734")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                            connectKafkaProvider(response);
                            Log.e(TAG, "prepareTopic: ");
                        },
                        throwable -> ShowLog(LogTypes.ERROR, throwable.getMessage())));

    }

    private static TopicResponse config;

    private void connectKafkaProvider(TopicResponse kafkaConfigs) {
        config = kafkaConfigs;
        provider = new KafkaDataProvider(kafkaConfigs, this);
    }

    private void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
        listener.hasError(message);
    }

    public void setListener(StreamEventListener listener) {
        this.listener = listener;
    }

    public void prepareStreaming(FileSetup file) {
        if(isReady) {
            file.setControlTopic(config.getControlTopic());
            file.setStreamTopic(config.getStreamTopic());
            api.getDashManifest(file.getUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(response -> {
                                fileReadyToPlay(response);
                            },
                            throwable -> ShowLog(LogTypes.ERROR, throwable.getMessage()));
        }

    }

    public void fileReadyToPlay(DashResponse response) {

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
        return new ProgressiveDataSource.Factory(response,provider);
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

    public void attachPlayer(DashResponse response) {
        if (isReady) {
            this.provider.startStreming(response);
            dataSourceFactory = buildDataSourceFactory(response);
            mediaSource = buildMediaSource();
            if (player != null) {
                player.addMediaSource(mediaSource);
                player.prepare();
                player.play();
            }
        } else {
            Log.e(TAG, "consumer in not ready: ");
        }
    }

    public void releasePlayer() {
        if (dataSourceFactory != null) {
            isReady = false;
            mCompositeDisposable.dispose();
            provider.release();
            player.stop(true);
            player = null;
        }
    }

    public void clean() {
        releasePlayer();
        instance = null;
    }

    @Override
    public void onStreamerIsReady(boolean state) {
        isReady = state;
        Log.e(TAG, "onStreamerIsReady: " + state);
    }
}
