package ir.fanap.podstreamsdkexample.data.remote;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;
import ir.fanap.podstream.offlinestream.PodStream;
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
        if (offlinestreamer == null) {
            offlinestreamer = PodStream.init(activity);
        }
    }

    public PodStream getOfflinestreamer() {
        return offlinestreamer;
    }

    public List<VideoItem> getVideo() {
        String[] localServerHashList =  {"YPAL2RJJWN7VCBUL","PLJC5LDWU3BC9PMU","TXSJRXENLD3BD8D7","296FF59BVT6M8OLW"};
        // String[] sandBoxServerHashList =  {"ZHBEOWS9PM2188E"}
      //  String[] mainServerHashList = {"5DLLXYSGNB7OQCRC", "6FVVFGQEPY4ZSF36", "ABYALWPAE1ZDO4BR"};
        String[] mainServerHashList = {"5DLLXYSGNB7OQCRC", "6FVVFGQEPY4ZSF36", "ZEK2ARE18AEU35AG", "DBPPGRD3PUPV4MNL"};
//        String[] mainServerHashList = {"DBPPGRD3PUPV4MNL" ,"87WVXWODLBAB79AC" ,"KPQZX1UNOL88UJZP" ,"AW3S5UBDAOMZMN2C"};
        List<VideoItem> items = new ArrayList<>();
        String[] names = {"video1", "video2", "test3", "video2", "test3"};
        String[] quality = {"320", "320", "320", "320", "320"};
        for (int i = 0; i < mainServerHashList.length; i++) {
            VideoItem item = new VideoItem();
            item.setVideoHash(mainServerHashList[i]);
            item.setVideoName(names[i]);
            item.setVideoQuality(quality[i]);
            items.add(item);
        }
        return items;
    }
}
