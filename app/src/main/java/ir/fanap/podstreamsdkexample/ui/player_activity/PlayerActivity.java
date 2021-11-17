package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.podstreamsdkexample.R;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public class PlayerActivity extends AppCompatActivity implements PlayerConstract.View {

    ConstraintLayout player_la;
    PlayerConstract.Presenter presenter;
    ProgressBar progressBar;
    Button bt_seek;
    TextView tvError;
    String selectedHash = "";
    private PlayerView playerView;
    List<VideoItem> response = new ArrayList<>();

    // If you want to make custom player view you need make custem view and attach it to your playerview
    // you can check below youtube link for make custom view.
    // https://www.youtube.com/watch?v=AejSubS3beY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        init();
    }

    int lastposition = 1000;

    private void initviews() {
        selectedHash = getIntent().getStringExtra("hash");
        player_la = findViewById(R.id.player_la);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        playerView = findViewById(R.id.player_view);
        bt_seek = findViewById(R.id.bt_seek);
        bt_seek.setOnClickListener(v -> {
            presenter.prepare(new FileSetup.Builder().build(getRandomHash()));
//            presenter.seekTo(lastposition);
        });
    }

    public String getRandomHash() {
        int random_int = (int) Math.floor(Math.random() * ((response.size()-1 )- 0 + 1) + 0);
        String randomHash = response.get(random_int).getVideoHash();
        if (randomHash.equals(getIntent().getStringExtra("hash"))) {
            random_int++;
            if (random_int == response.size())
                random_int = 0;
            randomHash = response.get(random_int).getVideoHash();
        }
        return randomHash;
    }

    public void init() {
        initviews();
        presenter = new PlayerPresenter(this, this);
        presenter.setPLayerView(playerView);

        presenter.prepare(new FileSetup.Builder().
                build(
                        selectedHash
                ));
    }


    void showLoading() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    void hideLoading() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStreamerReady(boolean state) {

    }

    @Override
    public void isLoading(boolean isloading) {
        if (isloading) {
            showLoading();
        } else {
            hideLoading();
        }
    }

    @Override
    public void hasError(String error) {
        Log.e("TAG", "hasError: " + error);
    }

    @Override
    public void timeOutHappend() {
        showError("Stresmer timeout was happend");
    }

    @Override
    public void onPlayerError() {
        showError("Player error was happend");
    }

    @Override
    public void onRecivedVideoList(List<VideoItem> response) {
        this.response = response;
    }
//
//    @Override
//    public void onReset(long position) {
//        lastposition = (int) position;
//        presenter.prepare(new FileSetup.Builder().
//                build(
//                        selectedHash));
//    }

    private void showError(String error) {
        tvError.append(error);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }


}