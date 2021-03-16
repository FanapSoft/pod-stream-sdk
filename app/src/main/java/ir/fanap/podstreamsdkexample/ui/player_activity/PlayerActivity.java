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

    ConstraintLayout player_la;
    PlayerConstract.Presenter presenter;
    ProgressBar progressBar;
    String selectedHash = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        init();
    }

    private void initviews() {
        selectedHash = getIntent().getStringExtra("hash");
        player_la = findViewById(R.id.player_la);
        progressBar = findViewById(R.id.progressBar);
    }

    public void init() {
        initviews();
        presenter = new PlayerPresenter(this, this);
        FileSetup file = new FileSetup.Builder().
                build(
                        "7936886af8064418b01e97f57c377734",
                        selectedHash
                );
        presenter.prepare(file);
    }

    @Override
    public void onFileReady() {

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


}