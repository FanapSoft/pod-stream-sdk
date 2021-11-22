package ir.fanap.podstream.offlinestream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.kafkassl.kafkaclient.ConsumResult;
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

import java.nio.ByteBuffer;

import io.reactivex.schedulers.Schedulers;
import ir.fanap.podstream.datasources.DataProvider;
import ir.fanap.podstream.datasources.FileDataSource;
import ir.fanap.podstream.datasources.ProgressiveDataSource;
import ir.fanap.podstream.datasources.buffer.EventListener;
import ir.fanap.podstream.kafka.KafkaClient;
import ir.fanap.podstream.kafka.KafkaClientManager;
import ir.fanap.podstream.model.ErrorOutPut;
import ir.fanap.podstream.model.FileSetup;
import ir.fanap.podstream.R;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.LogTypes;
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.Utils;
import ir.fanap.podstream.util.ssl.SSLHelper;
import ir.fanap.podstream.network.AppApi;
import ir.fanap.podstream.network.RetrofitClient;
import ir.fanap.podstream.network.response.TopicResponse;
import ir.fanap.podstream.util.HandlerMessageType.ActConstants;

public class PodStream implements KafkaClientManager.Listener, ProgressiveDataSource.DataSourceListener {

    public static String TAG = "PodStream";
    public static String KAFKALISTENERKEY = "main";
    @SuppressLint("StaticFieldLeak")
    private static PodStream instance;
    private StreamListener listener;
    private boolean showLog = false;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;
    public String token;
    private boolean isReady = false;
    private String End_Point_Base;
    private int backBufferSize = 180000;
    private Activity mContext;
    protected Gson gson;
    KafkaClientManager kafkaManager;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0L;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity) {
        if (instance == null) {
            instance = new PodStream();
            instance.setServer(activity);
            instance.setContext(activity);
            instance.netWorkInit();
            instance.kafkaManager = KafkaClientManager.getInstance(new SSLHelper(activity));
            instance.gson = new GsonBuilder().setPrettyPrinting().create();
        }
        return instance;
    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
    }

    private void setServer(Activity mContext) {
        End_Point_Base = mContext.getString(R.string.localserver);
    }

    /**
     * needs token fo connect to server for stream files
     **/
    public void setToken(String token) {
        this.token = token;
        kafkaManager.setToken(token);
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
                            connectKafkaProvider(response.getKafkaConcumerClient(), response.getKafkaProducerClient());
                        },
                        throwable -> {
                            ShowLog(LogTypes.ERROR, throwable.getMessage());
                            ShowLog(LogTypes.ERROR, throwable.toString());
                            errorHandle(Constants.TopicResponseErrorCode, throwable.getMessage());
                        });
    }

    private void connectKafkaProvider(KafkaClient consumerClient, KafkaClient producerClient) {
        kafkaManager.createProducer(producerClient);
        kafkaManager.createConsumer(consumerClient);
        kafkaManager.setListener(KAFKALISTENERKEY, this);
        kafkaManager.connectProducer();
        kafkaManager.connectConsumer();
        listener.onStreamerReady(true);
        isReady = true;
    }

    private void ShowLog(String logType, String message) {
        if (showLog)
            Log.e(TAG, logType + ": " + message);
    }

    private void errorHandle(int errorCode, String ErrorMesssage) {
        ErrorOutPut error = new ErrorOutPut(true, ErrorMesssage, errorCode);
        mHandler.obtainMessage(ActConstants.MESSAGE_ERROR, gson.toJson(error)).sendToTarget();
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
            kafkaManager.consume();
            kafkaManager.produceFileSizeMessage(file.getVideoAddress());
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

    private void fileReadyToPlay(long fileSize) {
        if (checkRequireds()) {
            releasePlayerResource();
            preparePlayer();
            mHandler.obtainMessage(ActConstants.MESSAGE_PLAYER_PREAPARE_TO_PLAY, fileSize).sendToTarget();
        } else {
            ShowLog("player", "Not Ready");
        }
    }

//    private FileDataSource.Factory buildDataSourceFactory(long s) {
//        return new FileDataSource.Factory(s);
//    }
//
//    private MediaSource buildMediaSource(long fileSize) {
//        FileDataSource.Factory dataSourceFactory = buildDataSourceFactory(fileSize);
//        MediaItem mediaItem = new MediaItem.Builder()
//                .setUri(Uri.EMPTY)
//                .build();
//        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
//    }

    private ProgressiveDataSource.Factory buildDataSourceFactory(long fileSize) {
        return new ProgressiveDataSource.Factory(fileSize).setDataSourcelistener(this);
    }

    private MediaSource buildMediaSource(long fileSize) {
        ProgressiveDataSource.Factory dataSourceFactory = buildDataSourceFactory(fileSize);
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.EMPTY)
                .build();
        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
    }

    public void setShowLog(boolean showLogs) {
        showLog = showLogs;
    }

    /**
     * set  {@link #currentWindow}. to default value (0)
     * set  {@link #playbackPosition}. to default value (0)
     * send message to {@link #mHandler} for init player
     */
    private void preparePlayer() {
        currentWindow = 0;
        playbackPosition = 0L;
        mHandler.obtainMessage(ActConstants.MESSAGE_PLAYER_INIT).sendToTarget();
    }

    /**
     * send message to {@link #mHandler} for release player
     */
    public void releasePlayerResource() {
        mHandler.obtainMessage(ActConstants.MESSAGE_PLAYER_RELEASE).sendToTarget();
    }

    public void stopStreaming() {
        if (kafkaManager.isStreaming())
            kafkaManager.stopConsume();
    }

    /**
     *
     **/
    private void releasePlayer() {
        try {
            if (player != null) {
                playWhenReady = player.getPlayWhenReady();
                currentWindow = player.getCurrentWindowIndex();
                playbackPosition = player.getCurrentPosition();
                Log.e("'TAG'", "releasePlayer: " + playWhenReady + "   --->   " + currentWindow + "   --->   " + "   --->   " + playbackPosition);
                player.release();
                player = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     *
     **/
    public void clean() {
        releasePlayerResource();
        kafkaManager.closeProducer();
        kafkaManager.closeConsumer();
        instance = null;
        isReady = false;
    }

    //ExoPlayer instances must be accessed from a single application thread. For the vast majority of cases this should be the application’s main thread.
    //https://exoplayer.dev/hello-world.html
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            @ActConstants int currentMessageType = message.what;
            switch (currentMessageType) {
                case ActConstants.MESSAGE_PLAYER_RELEASE:
                    releasePlayer();
                    break;
                case ActConstants.MESSAGE_PLAYER_INIT:
                    initPlayer(mContext);
                    break;
                case ActConstants.MESSAGE_PLAYER_PREAPARE_TO_PLAY:
                    MediaSource mediaSource = buildMediaSource(Long.parseLong(message.obj.toString()));
                    if (player != null) {
                        player.addMediaSource(mediaSource);
                        player.setPlayWhenReady(playWhenReady);
                        player.seekTo(currentWindow, playbackPosition);
                        player.prepare();
                    }
                    break;
                case ActConstants.MESSAGE_ERROR:
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

    @Override
    public void onFileReady(long fileSize) {
        fileReadyToPlay(fileSize);
        Log.e(TAG, "onFileReady: " + fileSize);
    }

    @Override
    public void onError(int code, String error) {
        Log.e(TAG, "onError: " + error);
        errorHandle(Constants.StreamerError, error);
    }
}

