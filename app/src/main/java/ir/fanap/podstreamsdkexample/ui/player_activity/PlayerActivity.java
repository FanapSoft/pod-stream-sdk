package ir.fanap.podstreamsdkexample.ui.player_activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.podstreamsdkexample.R;


import ir.fanap.podstream.Entity.FileSetup;

public class PlayerActivity extends AppCompatActivity implements PlayerConstract.View {

    ConstraintLayout player_la;
    PlayerConstract.Presenter presenter;
    ProgressBar progressBar;
    TextView tvError;
    String selectedHash = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        init();
    }
    private void test() {
        Log.e("TAG1", "start " );
        CountDownTimer timer = new CountDownTimer(1000,30000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("TAG1", "hello " );
                tvError.setText(millisUntilFinished + "");
            }

            @Override
            public void onFinish() {
                Log.e("TAG1", "done " );
            }
        };
        timer.start();
    }

    private void initviews() {
        selectedHash = getIntent().getStringExtra("hash");
        player_la = findViewById(R.id.player_la);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        test();
    }

    public void init() {
        initviews();
        presenter = new PlayerPresenter(this, this);
        FileSetup file = new FileSetup.Builder().
                build(
                        selectedHash
                );
        presenter.prepare(file,this);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  ext_error.append(error + "------\n");
            }
        });
    }

    @Override
    public void timeOutHappend() {
        showError("Stresmer timeout was happend");
    }

    @Override
    public void onPlayerError() {
        showError("Player error was happend");
    }

    private void showError(String error){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvError.append(error);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }


}