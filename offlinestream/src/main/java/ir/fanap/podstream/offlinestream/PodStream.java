package ir.fanap.podstream.offlinestream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;

import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.datasources.FileDataSource;
import ir.fanap.podstream.datasources.KafkaDataProvider;
import ir.fanap.podstream.datasources.ProgressiveDataSource;
import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.R;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.LogTypes;
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.ssl.SSLHelper;
import ir.fanap.podstream.network.AppApi;
import ir.fanap.podstream.network.RetrofitClient;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

public class PodStream implements KafkaDataProvider.Listener {

    public static String TAG = "PodStream";
    @SuppressLint("StaticFieldLeak")
    private static PodStream instance;
    private TopicResponse config;
    private Activity mContext;
    private StreamHandler.StreamEventListener listener;
    private boolean showLog = false;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;
    public String token;
    private boolean isReady = false;
    private ProgressiveDataSource.Factory dataSourceFactory;
    private KafkaDataProvider provider;
    private SSLHelper sslHelper;
    private String End_Point_Base;
    private boolean isCheck = false;
    private int backBufferSize = 3000;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity, String token) {
        if (instance == null) {
            instance = new PodStream();
            instance.setServer(activity);
            instance.setContext(activity);
            instance.netWorkInit(activity);
        }
        return instance;
    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
        if (sslHelper != null) {
            return;
        }
        sslHelper = new SSLHelper();
        try {
            sslHelper.generateFile(Constants.CERT_FILE, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setServer(Activity mContext) {
        End_Point_Base = mContext.getString(R.string.mainserver);
    }

    /**
     * needs token fo connect to server for stream files
     **/
    public void setToken(String token) {
        this.token = token;
        prepareTopic();
    }

    public void setBackBufferSize(int bufferSize) {
        this.backBufferSize = bufferSize;
    }

    private void initPlayer(Activity activity) {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        builder.setBackBuffer(backBufferSize, true);
        builder.setTargetBufferBytes(backBufferSize);

        player = new SimpleExoPlayer.Builder(activity).setLoadControl(builder.build()).build();
        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
                ShowLog(LogTypes.PLAYERSTATE, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(@NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {
                ShowLog(LogTypes.PLAYERSTATE, "onTracksChanged");
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                ShowLog(LogTypes.PLAYERSTATE, "onIsLoadingChanged");
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                playerView.hideController();
                listener.onIsLoadingChanged(state == ExoPlayer.STATE_BUFFERING);
                ShowLog(LogTypes.PLAYERSTATE, "onPlaybackStateChanged" + state);
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                ShowLog(LogTypes.PLAYERERROR, "onPlayerError" + error.errorCode + " " + error.getMessage());
                refreshPlayer();
            }
        });

        playerView.setPlayer(player);

    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    ;


    private void netWorkInit(Activity activity) {
        api = RetrofitClient.getInstance(activity.getString(R.string.mainserver)).create(AppApi.class);
    }

    private String getTopicUrl() {
        return End_Point_Base + "getTopic/?clientId=" + token;
    }

    private void prepareTopic() {
        api.getTopics(getTopicUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                            response.setsslPath(sslHelper.getCart().getAbsolutePath());
                            connectKafkaProvider(response);
                        },
                        throwable -> {
                            ShowLog(LogTypes.ERROR, throwable.getMessage());
                            ShowLog(LogTypes.ERROR, throwable.toString());
                            errorHandle(Constants.TopicResponseErrorCode, throwable.getMessage());
                        });
    }

    private void connectKafkaProvider(TopicResponse kafkaConfigs) {
        config = kafkaConfigs;
        provider = new KafkaDataProvider(kafkaConfigs, this);
    }

    private void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
    }

    private void errorHandle(int errorCode, String ErrorMesssage) {
        if (listener != null) {
            listener.hasError(ErrorMesssage, errorCode);

        }
    }


    /**
     *
     **/
    public void setListener(StreamHandler.StreamEventListener listener) {
        this.listener = listener;
    }


    public void prepareStreaming(FileSetup file, PlayerView playerView) {
        if (isReady) {
            instance.playerView = playerView;
            if (isCheck) {
                new PodThreadManager().doThisAndGo(new Runnable() {
                    @Override
                    public void run() {
                        provider.prepareDashFileForPlay(file.getVideoAddress(), token);
                    }
                });
            } else {
                file.setControlTopic(config.getControlTopic());
                file.setStreamTopic(config.getStreamTopic());
                api.getDashManifest(file.getUrl(End_Point_Base, token))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(response -> {
                                    fileReadyToPlay(response);
                                    this.response = response;
                                    isCheck = true;
                                },
                                throwable -> {
                                    ShowLog(LogTypes.ERROR, throwable.getMessage());
                                    errorHandle(Constants.StreamerResponseErrorCode, throwable.getMessage());
                                });
            }
        }
    }

    DashResponse response;

    private void fileReadyToPlay(DashResponse response) {
        mContext.runOnUiThread(() -> attachPlayer(response));
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
                .build();
        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
    }

    public void setShowLog(boolean showLogs) {
        showLog = showLogs;
    }

    private void attachPlayer(DashResponse response) {
        if (isReady) {
            setBackBufferSize((int) response.getSize());
            initPlayer(mContext);
            provider.startStreming(response);
            dataSourceFactory = buildDataSourceFactory(response);
            MediaSource mediaSource = buildMediaSource();
            if (player != null) {
                player.addMediaSource(mediaSource);
                player.prepare();
                player.play();
            }
        } else {
            ShowLog("player", "Not Ready");
        }
    }

    /**
     *
     **/
    public void releasePlayer() {
        try {
            if (dataSourceFactory != null) {
                player.stop();
                playerView.removeAllViews();
                playerView.setPlayer(null);
                player.release();
                provider.stopStreaming();
                player = null;
                dataSourceFactory = null;
                playerView = null;
            }
        } catch (Exception e) {
            ShowLog("player", "Player released");
        }
    }

    /**
     *
     **/
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
    }

    @Override
    public void onFileReady(DashResponse dashFile) {
        fileReadyToPlay(dashFile);
        this.response = dashFile;
    }

    @Override
    public void onTimeOut() {
        errorHandle(Constants.TimeOutStreamer, "StreamerTimeOut");
        releasePlayer();
        listener.onReset(null);
    }

    @Override
    public void onError(String message) {
        errorHandle(Constants.StreamerError, message);
        releasePlayer();
        listener.onReset(null);
    }

    boolean isRefresh;

    // TODO Can be better
    //  we need a method which reset every thing automatically when has error
    private void refreshPlayer() {
        try {
            isRefresh = true;
            releasePlayer();
            player.release();
            player = null;
            isCheck = false;
            provider.release();
            isReady = false;
            onStreamerIsReady(false);
            prepareTopic();
        } catch (Exception _) {
            ShowLog(LogTypes.ERROR, _.toString());
        }
    }

}
