package ir.fanap.podstreamsdkexample.ui.splash_activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.podstreamsdkexample.R;
import com.google.android.exoplayer2.ui.PlayerView;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.ui.ListItem.VideoListActivity;
import ir.fanap.podstreamsdkexample.ui.ListItem.VideoListPresenter;

public class SplashActivity extends AppCompatActivity implements SplashConstract.View {

    SplashConstract.Presenter presenter;
    String token = "ed24e37c7ee84313acf2805a80122f94";
    TextView txt_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.splash_activity);
        init();
    }

    public void init() {
        txt_state = findViewById(R.id.txt_state);
        presenter = new SplashPresenter(this, this);
        presenter.init(token);
    }


    @Override
    public void onStreamerReady(boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_state.setText("Streame is ready");
            }
        });


        if (state) {
            startActivity(new Intent(this, VideoListActivity.class));
            finish();
        }

    }

}