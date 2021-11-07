package ir.fanap.podstream.offlinestream;

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
    private static PodStream instance;
    private TopicResponse config;
    private StreamHandler.StreamEventListener listener;
    private ProgressiveDataSource.Factory dataSourceFactory;
    private KafkaDataProvider provider;
    private SSLHelper sslHelper;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;
    public String token;
    private String End_Point_Base;
    private boolean isReady;
    private boolean showLog;
    private boolean isCheck;
    private int backBufferSize = 3000;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity) {
        if (instance == null) {
            instance = new PodStream();
            instance.setServer(activity);
            instance.initSslHelper(activity);
            instance.netWorkInit();
        }
        return instance;
    }


    private void initSslHelper(Activity mContext) {
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
            }
        });

        playerView.setPlayer(player);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    private void netWorkInit() {
        api = RetrofitClient.getInstance(End_Point_Base).create(AppApi.class);
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

    public void setPlayerView(PlayerView playerView, Activity activity) {
        this.playerView = playerView;
        initPlayer(activity);
    }

    public void prepareStreaming(FileSetup file, Activity activity) {
        if (!isReady) {
            errorHandle(Constants.StreamerError, "Streamer is not ready !!!");
            return;
        }

        if (playerView == null) {
            errorHandle(Constants.StreamerError, "PlayerView is null !!!");

            return;
        }

        if (isCheck) {
            new PodThreadManager().doThisAndGo(new Runnable() {
                @Override
                public void run() {
                    provider.prepareDashFileForPlay(file.getVideoAddress(), token, activity);
                }
            });
        } else {
            file.setControlTopic(config.getControlTopic());
            file.setStreamTopic(config.getStreamTopic());
            api.getDashManifest(file.getUrl(End_Point_Base, token))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(response -> {
                                fileReadyToPlay(activity, response);
                                isCheck = true;
                            },
                            throwable -> {
                                ShowLog(LogTypes.ERROR, throwable.getMessage());
                                errorHandle(Constants.StreamerResponseErrorCode, throwable.getMessage());
                            });
        }
    }


    private void fileReadyToPlay(Activity mContext, DashResponse response) {
        if (isReady) {
            attachPlayer(mContext, response);
        } else {
            ShowLog("player", "Not Ready");
        }
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


    public void attachPlayer(Activity mContext, DashResponse response) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    releasePlayer();
                    initPlayer(mContext);
                }
                provider.startStreming(response);
                if (dataSourceFactory == null)
                    dataSourceFactory = buildDataSourceFactory(response);
                MediaSource mediaSource = buildMediaSource();
                if (player != null) {
                    player.addMediaSource(mediaSource);
                    player.prepare();
                    player.play();
                }
            }
        });
    }

    /**
     *
     **/
    public void releasePlayer() {
        if (player != null) {
            player.stop();
            player.release();
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (provider != null) {
            provider.stopStreaming();
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
    public void onFileReady(DashResponse dashFile, Activity activity) {
        fileReadyToPlay(activity, dashFile);
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
}
