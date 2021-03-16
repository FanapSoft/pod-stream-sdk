package ir.fanap.podstreamsdkexample.data.remote;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstream.offlineStream.PodStream;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public class Repository {
    private static Repository instance = null;
    PodStream offlinestreamer;

    public synchronized static Repository getInstance() {
        if (instance == null)
            instance = new Repository();

        return instance;
    }

    public void Streamer(Activity activity) {
        if (offlinestreamer == null)
            offlinestreamer = PodStream.init(activity);
    }

    public PodStream getOfflinestreamer() {
        return offlinestreamer;
    }

    public List<VideoItem> getVideo() {
        List<VideoItem> items = new ArrayList<>();


// "I11TQ7KAY4ZWQ3E", "U6EG9YVLL6XZ1DR", "ZHBEOWS9PM2188E",
        String[] videoHashcodes = {"9ZHBEOWS9PM2188E", "XEALFVCIVPZDI4EJ"};
        String[] names = {"video1", "video2"};
        String[] quality = {"320", "320"};
        for (int i = 0; i < videoHashcodes.length; i++) {
            VideoItem item = new VideoItem();
            item.setVideoHash(videoHashcodes[i]);
            item.setVideoName(names[i]);
            item.setVideoQuality(quality[i]);
            items.add(item);
        }
        return items;
    }
}
