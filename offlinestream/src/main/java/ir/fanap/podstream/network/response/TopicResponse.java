package ir.fanap.podstream.network.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TopicResponse implements Serializable {

    @Expose
    @SerializedName("controlTopic")
    private String controlTopic;

    @Expose
    @SerializedName("streamTopic")
    private String streamTopic;

    //private String brokerAddress = "192.168.112.32:9092";

    public TopicResponse(String controlTopic, String streamTopic) {
        this.controlTopic = controlTopic;
        this.streamTopic = streamTopic;
    }

    public String getControlTopic() {
        return controlTopic;
    }

    public TopicResponse setControlTopic(String controlTopic) {
        this.controlTopic = controlTopic;
        return this;
    }

    public String getStreamTopic() {
        return streamTopic;
    }

    public TopicResponse setStreamTopic(String streamTopic) {
        this.streamTopic = streamTopic;
        return this;
    }

    public String toString(TopicResponse response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        return json;
    }

    public String getBrokerAddress() {
        return "192.168.112.32:9092";
    }
}
