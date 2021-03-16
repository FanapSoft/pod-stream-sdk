package ir.fanap.podstream.network;

import io.reactivex.Observable;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface AppApi {
    @GET
    Observable<DashResponse> getDashManifest(@Url String url);

    @GET
    Observable<TopicResponse> getTopics(@Url String url);
}