package ir.fanap.podstreamsdkexample.ui.splash_activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.podstreamsdkexample.R;

import ir.fanap.podstreamsdkexample.ui.ListItem.VideoListActivity;

public class SplashActivity extends AppCompatActivity implements SplashConstract.View {

    SplashConstract.Presenter presenter;
//    String token = "349157fdca054d1f8fdb78f2a57964c5";
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

    @Override
    public void onError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        txt_state.setText(error);
    }

}