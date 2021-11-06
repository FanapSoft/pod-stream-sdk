package ir.fanap.podstream.offlineStream;

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
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.MimeTypes;

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
    @SuppressLint("StaticFieldLeak")
    private static PodStream instance;
    private TopicResponse config;
    private Activity mContext;
    private StreamHandler.StreamEventListener listener;
    private boolean showLog = false;
    private StyledPlayerView playerView;
    private SimpleExoPlayer player;
    private AppApi api;
    public String token;
    private boolean isReady = false;
    private ProgressiveDataSource.Factory dataSourceFactory;
    private KafkaDataProvider provider;
    private SSLHelper sslHelper;
    private String End_Point_Base;
    boolean isCheck = false;

    private PodStream() {

    }

    public synchronized static PodStream init(Activity activity, String token) {
        if (instance == null) {
            instance = new PodStream();
            instance.setServer(activity);
            instance.setContext(activity);
            instance.netWorkInit(activity);
            instance.initPlayer(activity);
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

    private void setServer(Activity mContext) {
        End_Point_Base = mContext.getString(R.string.localserver);
    }

    /**
     * needs token fo connect to server for stream files
     **/
    public void setToken(String token) {
        this.token = token;
        prepareTopic();
    }

    private void initPlayer(Activity activity) {
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        builder.setBackBuffer(10000, true);
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
//                refreshPlayer();
            }
        });
    }


    private void netWorkInit(Activity activity) {
        api = RetrofitClient.getInstance(activity.getString(R.string.localserver)).create(AppApi.class);
        mCompositeDisposable = new CompositeDisposable();
    }

    private String getTopicUrl() {
        return End_Point_Base + "getTopic/?clientId=" + token;
    }

    private void prepareTopic() {
        mCompositeDisposable.add(api.getTopics(getTopicUrl())
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
                        }));
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

    public void prepareStreaming(FileSetup file, Activity activity) {
        if (isReady) {
            instance.playerView = activity.findViewById(R.id.player_view);
            if (playerView.getPlayer() != player)
                playerView.setPlayer(player);
            if (isCheck) {
                Thread t = new Thread(() -> provider.prepareDashFileForPlay(file.getVideoAddress(), token));
                t.start();
            } else {
                file.setControlTopic(config.getControlTopic());
                file.setStreamTopic(config.getStreamTopic());
                mCompositeDisposable.add(api.getDashManifest(file.getUrl(End_Point_Base, token))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(response -> {
                                    fileReadyToPlay(response);
                                    isCheck = true;
                                },
                                throwable -> {
                                    ShowLog(LogTypes.ERROR, throwable.getMessage());
                                    errorHandle(Constants.StreamerResponseErrorCode, throwable.getMessage());
                                }));
            }
        }
    }


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
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build();
        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mediaItem);
    }

    public void disableLogs() {
        showLog = false;
    }

    private void attachPlayer(DashResponse response) {
        if (isReady) {
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
                mCompositeDisposable.dispose();
                player.stop();
                playerView.removeAllViews();
                dataSourceFactory = null;
                player.clearVideoSurface();
                player.clearMediaItems();
                provider.stopStreaming();
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
    }

    @Override
    public void onTimeOut() {
        errorHandle(Constants.TimeOutStreamer, "StreamerTimeOut");
//        refreshPlayer();
    }

    @Override
    public void onError(String message) {
        errorHandle(Constants.StreamerError, message);
//        refreshPlayer();
    }

    // TODO Can be better
    //  we need a method which reset every thing automatically when has error
    private void refreshPlayer() {
        try {
            releasePlayer();
            player.release();
            player = null;
            provider.release();
//            isReady = false;
//            instance.initPlayer(mContext);
//            onStreamerIsReady(false);
            prepareTopic();
        } catch (Exception _) {

        }
    }
}
