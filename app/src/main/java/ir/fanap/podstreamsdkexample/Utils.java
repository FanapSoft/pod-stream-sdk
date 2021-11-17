package ir.fanap.podstreamsdkexample;

import android.content.Context;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import ir.fanap.podstreamsdkexample.data.VideoItem;

public class Utils {

    private static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("hashlist.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    public static List<VideoItem> getMainVideoList(Context context) {
        List<VideoItem> videolist = null;
        try {
            JSONObject obj = new JSONObject(Objects.requireNonNull(loadJSONFromAsset(context)));
            JSONArray m_jArry = obj.getJSONArray("main");
            videolist = new Gson().fromJson(m_jArry.toString(), new TypeToken<List<VideoItem>>() {
            }.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videolist;
    }

    public static List<VideoItem> getSandBoxVideoList(Context context) {
        List<VideoItem> videolist = null;
        try {
            JSONObject obj = new JSONObject(Objects.requireNonNull(loadJSONFromAsset(context)));
            JSONArray m_jArry = obj.getJSONArray("sandbox");
            videolist = new Gson().fromJson(m_jArry.toString(), new TypeToken<List<VideoItem>>() {
            }.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videolist;
    }

    public static List<VideoItem> getLocalVideoList(Context context) {
        List<VideoItem> videolist = null;
        try {
            JSONObject obj = new JSONObject(Objects.requireNonNull(loadJSONFromAsset(context)));
            JSONArray m_jArry = obj.getJSONArray("local");
            videolist = new Gson().fromJson(m_jArry.toString(), new TypeToken<List<VideoItem>>() {
            }.getType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videolist;
    }


}
