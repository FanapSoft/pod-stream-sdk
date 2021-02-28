package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.podstreamsdkexample.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;
import ir.fanap.podstreamsdkexample.ui.base.custom.VideoListAdaper;

public class PlayerActivity extends AppCompatActivity implements PlayerConstract.View {

    RecyclerView recycler_medialist;
    ConstraintLayout player_la;
    PlayerConstract.Presenter presenter;
    ProgressBar progressBar;
    VideoListAdaper videoListAdaper;

    boolean start = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        init();
    }

    private void initviews() {
        recycler_medialist = findViewById(R.id.recycler_medialist);
        recycler_medialist.setLayoutManager(new LinearLayoutManager(this));
        player_la = findViewById(R.id.player_la);
        progressBar = findViewById(R.id.progressBar);
    }

    public void init() {
        initviews();
        presenter = new PlayerPresenter(this, this);

        videoListAdaper = new VideoListAdaper(new ArrayList<>(), this);
        videoListAdaper.setmClickListener(this::onItemClick);
        recycler_medialist.setAdapter(videoListAdaper);


        presenter.getVideoList();
    }

    @Override
    public void onFileReady() {

    }

    void showLoading() {

        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }

    }
    Date startT  ;
    void hideLoading() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            Log.e("StartDelay", "give response: " + (new Date().getTime() - startT.getTime()));

            progressBar.setVisibility(View.INVISIBLE);
        }
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  ext_error.append(error + "------\n");
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }

    @Override
    public void onRecivedVideoList(List<VideoItem> response) {
        videoListAdaper.setDataList(response);
    }


    public void onItemClick(VideoItem item) {
        isLoading(true);
        if (start) {
            start = false;
            player_la.setVisibility(View.VISIBLE);
        }
        Log.e("StartDelay", "gotoplay: ");

        startT = new Date();
        prepareToPlayVideo(item);
    }

    void prepareToPlayVideo(VideoItem item) {
        FileSetup file = new FileSetup.Builder().
                build(
                        "7936886af8064418b01e97f57c377734",
                        item.getVideoHash()
                );
        presenter.prepare(file);
    }
}