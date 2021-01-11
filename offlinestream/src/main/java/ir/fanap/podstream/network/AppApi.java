package ir.fanap.podstream.network;

import io.reactivex.Observable;
import ir.fanap.podstream.network.response.DashResponse;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface AppApi {
    @GET
    Observable<DashResponse> getDashManifest(@Url String url);
}