package ir.fanap.podstream.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DashResponse implements Serializable,AvoidObfuscate {

    @Expose
    @SerializedName("consumTopic")
    private String consumTopic;

    @Expose
    @SerializedName("produceTopic")
    private String produceTopic;

    @Expose
    @SerializedName("brokerAddress")
    private String brokerAddress;

    @Expose
    @SerializedName("size")
    private long size;

    public String getConsumTopic() {
        return consumTopic;
    }

    public String getProduceTopic() {
        return produceTopic;
    }

    public String getBrokerAddress() {
        return brokerAddress;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
