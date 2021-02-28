package ir.fanap.podstreamsdkexample.data.remote;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstreamsdkexample.data.VideoItem;

public class Repository {
    private static Repository instance = null;

    public synchronized static Repository getInstance() {
        if (instance == null)
            instance = new Repository();

        return instance;
    }


    public List<VideoItem> getVideo() {
        List<VideoItem> items = new ArrayList<>();
        String[] videoHashcodes = {"2MUMAFQQ6MKQFK84", "A4LGHTOURUCII46D"};
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
