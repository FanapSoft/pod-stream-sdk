package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        init();

    }

    private void test() {
        Log.e("TAG1", "start " );
        CountDownTimer timer = new CountDownTimer(20000,5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("TAG1", "hello " );
            }

            @Override
            public void onFinish() {
                Log.e("TAG1", "done " );
            }
        };
        timer.start();
    }

    private void initviews() {
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