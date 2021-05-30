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

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.DataSources.FileDataSource;
import ir.fanap.podstream.DataSources.KafkaDataProvider;
import ir.fanap.podstream.DataSources.ProgressiveDataSource;
import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstream.R;
import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.Util.LogTypes;
import ir.fanap.podstream.Util.ssl.SSLHelper;
import ir.fanap.podstream.network.AppApi;
import ir.fanap.podstream.network.RetrofitClient;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

public class PodStream implements KafkaDataProvider.Listener {
    public static String TAG = "PodStream";
    private CompositeDisposable mCompositeDisposable;
    private static PodStream instance;
    private Activity mContext;
    private Gson gson;
    private StreamEventListener listener;
    private boolean showLog = true;
    private StyledPlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;
    public String token = "193e8f07232546d6ac0d56784ff91c41";

    private boolean isReady = false;
    private ProgressiveDataSource.Factory dataSourceFactory;
    //    private FileDataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    private KafkaDataProvider provider;
    private SSLHelper sslHelper;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity,String token) {

        if (instance == null) {
            instance = new PodStream();
            instance.setContext(activity);
            instance.netWorkInit();
            instance.setToken(token);
            //  instance.initPlayer(activity);
//            instance.prepareTopic();
        }

        return instance;

    }

    private void setContext(Activity mContext) {
        this.mContext = mContext;
        sslHelper = new SSLHelper();
        try {
            sslHelper.generateFile(Constants.CERT_FILE, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToken(String token) {
       this.token = token;
    }


    public void initPlayer(Activity activity) {
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
        Log.e(TAG, "initPlayer: ");
    }

    private void netWorkInit() {
        api = RetrofitClient.getInstance().create(AppApi.class);
        gson = new GsonBuilder().setPrettyPrinting().create();
        mCompositeDisposable = new CompositeDisposable();
    }

    public void prepareTopic() {
        mCompositeDisposable.add(api.getTopics(Constants.End_Point_Topic + token)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                            response.setsslPath(sslHelper.getCart().getAbsolutePath());
                            connectKafkaProvider(response);
                            Log.e(TAG, "get topic: ");
                        },
                        throwable -> {
                            Log.e(TAG, "can not get topic: ");
                            ShowLog(LogTypes.ERROR, throwable.getMessage());
                        }));

    }

    private static TopicResponse config;

    private void connectKafkaProvider(TopicResponse kafkaConfigs) {
        config = kafkaConfigs;
        provider = new KafkaDataProvider(kafkaConfigs, this);
    }

    private void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
        if (listener != null)
            listener.hasError(message);
    }

    public void setListener(StreamEventListener listener) {
        this.listener = listener;
    }

    public void prepareStreaming(FileSetup file) {
        if (isReady) {
            if (isCheck) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        provider.prepareDashFileForPlay(file.getVideoAddress(), token);
                    }
                });
                t.start();
            } else {
                file.setControlTopic(config.getControlTopic());
                file.setStreamTopic(config.getStreamTopic());
                api.getDashManifest(file.getUrl(token))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(response -> {
                                    fileReadyToPlay(response);
                                    isCheck = true;
                                    Log.e(TAG, "ready  ");
                                },
                                throwable -> {
                                    ShowLog(LogTypes.ERROR, throwable.getMessage());
                                    Log.e(TAG, "error on play ");
                                });
            }
        }
    }

    void test() {

    }

    boolean isCheck = false;

    private void fileReadyToPlay(DashResponse response) {

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
        return new ProgressiveDataSource.Factory(response, provider);
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

    private void attachPlayer(DashResponse response) {
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
        try {
            if (dataSourceFactory != null) {
                mCompositeDisposable.dispose();
                player.stop(true);
                player = null;
                Log.e(TAG, "releasePlayer: ");
            }
        } catch (Exception e) {
            Log.e(TAG, "exeption: ");
        }
    }

    public void clean() {
        releasePlayer();
        provider.release();
        instance = null;
        isReady = false;
    }

    @Override
    public void onStreamerIsReady(boolean state) {
        isReady = state;
        listener.onStreamerReady(state);
        Log.e(TAG, "onStreamerIsReady: " + state);
    }

    @Override
    public void onFileReady(DashResponse dashFile) {
        fileReadyToPlay(dashFile);
    }
}
