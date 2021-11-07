package ir.fanap.podstream.network.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TopicResponse implements Serializable, AvoidObfuscate {

    @Expose
    @SerializedName("controlTopic")
    private String controlTopic;

    @Expose
    @SerializedName("streamTopic")
    private String streamTopic;

    @Expose
    @SerializedName("brokerAddress")
    private String brokerAddress;


    @Expose
    @SerializedName("size")
    private long size;

    private String sslPath;

    public String getSslPath() {
        return sslPath;
    }

    public void setsslPath(String sslPath) {
        this.sslPath = sslPath;
    }

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

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public String getBrokerAddress() {
        return "188.75.65.122:9092,188.75.65.122:9093";
//        return "192.168.112.32";
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }
}
