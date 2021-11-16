package ir.fanap.podstream.offlinestream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.datasources.FileDataSource;
import ir.fanap.podstream.datasources.KafkaDataProvider;
import ir.fanap.podstream.datasources.ProgressiveDataSource;
import ir.fanap.podstream.entity.ErrorOutPut;
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
    private StreamListener listener;
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
    private int backBufferSize = 3000;
    private Activity mContext;
    private DashResponse response;
    protected Gson gson;
    private FileSetup currentFile = null;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity) {
        if (instance == null) {
            instance = new PodStream();
            instance.setServer(activity);
            instance.setContext(activity);
            instance.initSslHelper(activity);
            instance.netWorkInit();
            instance.gson = new GsonBuilder().setPrettyPrinting().create();
        }
        return instance;
    }


    public void setContext(Activity mContext) {
        this.mContext = mContext;
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
        End_Point_Base = mContext.getString(R.string.sandboxserver);
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


    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0L;

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
        provider = new KafkaDataProvider(kafkaConfigs, this);
    }

    private void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
    }

    private void errorHandle(int errorCode, String ErrorMesssage) {
        ErrorOutPut error = new ErrorOutPut(true, ErrorMesssage, errorCode);
        mHandler.obtainMessage(3, gson.toJson(error)).sendToTarget();
    }

    /**
     *
     **/
    public void setListener(StreamListener listener) {
        this.listener = listener;
    }

    public void setPlayerView(PlayerView playerView) {
        this.playerView = playerView;
    }

    public void prepareStreaming(FileSetup file) {
        if (checkRequireds()) {
            currentFile = file;
            new PodThreadManager().doThisAndGo(() -> provider.prepareDashFileForPlay(file, token));
        }
    }


    public void refreshStreaming(FileSetup file) {
        if (checkRequireds()) {
            new PodThreadManager().doThisAndGo(() -> provider.prepareDashFileForPlay(file, token));
        }
    }


    private boolean checkRequireds() {
        if (!isReady) {
            errorHandle(Constants.StreamerError, "Not Ready");
            return false;
        } else if (mContext == null) {
            errorHandle(Constants.StreamerError, "Context is null");
            return false;
        } else if (token == null) {
            errorHandle(Constants.StreamerError, "Token is null");
            return false;
        }
        return true;
    }

    private void fileReadyToPlay() {
        if (isReady) {
            release();
            initPlayer();
            mHandler.obtainMessage(2, "start to streaming !!!").sendToTarget();
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

    public void initPlayer() {
        currentWindow = 0;
        playbackPosition = 0L;
        mHandler.obtainMessage(1).sendToTarget();
    }

    public void release() {
        mHandler.obtainMessage(0).sendToTarget();
    }

    /**
     *
     **/
    public void releasePlayer() {
        try {
            if (player != null) {
                playWhenReady = player.getPlayWhenReady();
                currentWindow = player.getCurrentWindowIndex();
                playbackPosition = player.getCurrentPosition();
                Log.e("'TAG'", "releasePlayer: " + playWhenReady + "   --->   " + currentWindow + "   --->   " + "   --->   " + playbackPosition);
                player.release();
                player = null;
            }
            if (provider != null)
                provider.stopStreaming();
        } catch (Exception e) {
        }
    }

    /**
     *
     **/
    public void clean() {
        release();
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
        this.response = dashFile;
        fileReadyToPlay();
    }

    @Override
    public void onTimeOut() {
        errorHandle(Constants.TimeOutStreamer, "StreamerTimeOut");
        if (player != null) {
            refreshStreaming(currentFile);
        }
    }

    @Override
    public void onError(String message) {
        errorHandle(Constants.StreamerError, message);
//        if (player != null) {
//            refreshStreaming(currentFile);
//        }
    }


    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    releasePlayer();
                    break;
                case 1:
                    initPlayer(mContext);
                    break;
                case 2:
                    provider.startStreming(response);
                    dataSourceFactory = buildDataSourceFactory(response);
                    MediaSource mediaSource = buildMediaSource();
                    if (player != null) {
                        player.addMediaSource(mediaSource);
                        player.setPlayWhenReady(playWhenReady);
                        player.seekTo(currentWindow, playbackPosition);
                        player.prepare();
                    }
                    break;
                case 3:
                    if (listener != null) {
                        try {
                            ErrorOutPut error = gson.fromJson(message.obj.toString(), ErrorOutPut.class);
                            String jsonError = gson.toJson(error);
                            listener.onError(jsonError, error);
                        } catch (Exception e) {
                            listener.onError(message.obj.toString(), new ErrorOutPut(true, message.obj.toString(), 18));
                        }
                    }
                    break;
            }
        }
    };
}

