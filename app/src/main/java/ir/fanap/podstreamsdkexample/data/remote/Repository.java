package ir.fanap.podstreamsdkexample.data.remote;

import android.app.Activity;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstreamsdkexample.Utils;
import ir.fanap.podstreamsdkexample.data.VideoItem;

public class Repository {
    private static Repository instance = null;
    PodStream offlinestreamer;

    public synchronized static Repository getInstance() {
        if (instance == null)
            instance = new Repository();
        return instance;
    }

    Activity activity;

    public void Streamer(Activity activity) {
        if (offlinestreamer == null) {
            this.activity = activity;
            offlinestreamer = PodStream.init(activity);
        }
    }

    public PodStream getOfflinestreamer() {
        return offlinestreamer;
    }

    int server = 2;

    // 0 == local  , 1 == main  ,  2 == sandbox
    public List<VideoItem> getVideo() {
        if (server == 0)
            return Utils.getLocalVideoList(activity);
        else if (server == 2)
            return Utils.getSandBoxVideoList(activity);
        else
            return Utils.getMainVideoList(activity);
    }
}
