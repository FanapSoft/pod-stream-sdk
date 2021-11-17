package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.podstreamsdkexample.R;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstreamsdkexample.data.VideoItem;
import ir.fanap.podstreamsdkexample.ui.base.custom.VideoListAdaper;
import ir.fanap.podstreamsdkexample.ui.player_activity.PlayerActivity;

public class VideoListActivity extends AppCompatActivity implements VideoListConstract.View {

    RecyclerView recycler_medialist;
    VideoListConstract.Presenter presenter;
    VideoListAdaper videoListAdaper;
    String token = "ed24e37c7ee84313acf2805a80122f94";
    boolean start = true;
    Button btn_add;
    EditText edt_name, edt_hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        init();

    }

    public void addVideo(String videoName, String videoHash) {
        if (videoHash.equals("") || videoHash.length() < 10) {
            Toast.makeText(this, "hash is ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (videoName.equals(""))
            videoName = "deafualt";
        videoListAdaper.addVideo(new VideoItem(videoName, videoHash, "320", "0"));
    }

    private void initviews() {
        btn_add = findViewById(R.id.btn_add);
        edt_name = findViewById(R.id.edt_name);
        edt_hash = findViewById(R.id.edt_hash);
        btn_add.setOnClickListener(v -> {
            addVideo(edt_name.getText().toString(), edt_hash.getText().toString());
        });
        recycler_medialist = findViewById(R.id.recycler_medialist);
        recycler_medialist.setLayoutManager(new LinearLayoutManager(this));
    }

    public void init() {
        initviews();
        presenter = new VideoListPresenter(this, this);
        videoListAdaper = new VideoListAdaper(new ArrayList<>(), this);
        videoListAdaper.setmClickListener(this::onItemClick);
        recycler_medialist.setAdapter(videoListAdaper);
        presenter.getVideoList();
        presenter.init(token);
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
//        if (state)
//            txt_state.setText("Streamer is Ready");
//        else
//            txt_state.setText("Streamer not Ready");
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