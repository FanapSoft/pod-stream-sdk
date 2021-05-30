package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.podstreamsdkexample.R;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstream.Entity.FileSetup;
import ir.fanap.podstreamsdkexample.data.VideoItem;
import ir.fanap.podstreamsdkexample.ui.base.custom.VideoListAdaper;
import ir.fanap.podstreamsdkexample.ui.player_activity.PlayerActivity;

public class VideoListActivity extends AppCompatActivity implements VideoListConstract.View {

    RecyclerView recycler_medialist;
    Button btn_settoken;
    EditText token_input;
    TextView txt_state;

    VideoListConstract.Presenter presenter;
    VideoListAdaper videoListAdaper;
    String token = "9b4139be20e845048b7f7db8e59e9bb7";
    boolean start = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        init();
    }

    private void initviews() {
        recycler_medialist = findViewById(R.id.recycler_medialist);
        token_input = findViewById(R.id.token_input);
        btn_settoken = findViewById(R.id.btn_settoken);
        txt_state = findViewById(R.id.txt_state);
        recycler_medialist.setLayoutManager(new LinearLayoutManager(this));
        token_input.setText(token);
        btn_settoken.setOnClickListener(view -> {
            if (!token_input.getText().toString().equals("null")) {
                token = token_input.getText().toString();
                presenter.init(token);
            } else
                Toast.makeText(this, "please input a token", Toast.LENGTH_SHORT).show();
        });
    }

    public void init() {
        initviews();
        presenter = new VideoListPresenter(this, this);
        videoListAdaper = new VideoListAdaper(new ArrayList<>(), this);
        videoListAdaper.setmClickListener(this::onItemClick);
        recycler_medialist.setAdapter(videoListAdaper);
        presenter.getVideoList();
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

    @Override
    public void onStreamerReady(boolean state) {
        Log.e("TAG", "onStreamerReady: ");
        if (state)
            txt_state.setText("Streamer is Ready");
        else
            txt_state.setText("Streamer not Ready");
    }


    public void onItemClick(VideoItem item) {
        if (start) {
            start = false;
        }
        Log.e("StartDelay", "gotoplay: ");
        prepareToPlayVideo(item);
    }

    void prepareToPlayVideo(VideoItem item) {

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("hash", item.getVideoHash());
        startActivity(intent);
    }
}