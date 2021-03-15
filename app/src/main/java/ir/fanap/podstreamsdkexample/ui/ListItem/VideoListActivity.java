package ir.fanap.podstreamsdkexample.ui.ListItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
    VideoListConstract.Presenter presenter;
    VideoListAdaper videoListAdaper;

    boolean start = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_activity);
        init();
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
        if (start) {
            start = false;
        }
        Log.e("StartDelay", "gotoplay: ");

        prepareToPlayVideo(item);
    }

    void prepareToPlayVideo(VideoItem item) {
        FileSetup file = new FileSetup.Builder().
                build(
                        "7936886af8064418b01e97f57c377734",
                        item.getVideoHash()
                );
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("hash",item.getVideoHash());
        startActivity(intent);
    }
}