package ir.fanap.podstream.network.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import ir.fanap.podstream.kafka.KafkaClient;

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


    public String toString() {
        return new Gson().toJson(this);
    }

    public String getBrokerAddress() {
//        return "192.168.112.32";
        return brokerAddress;
    }

    public void setBrokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
    }

    public KafkaClient getKafkaProducerClient() {
        return new KafkaClient.Builder().setTopic(controlTopic).setBrokerAddress(brokerAddress).build();
    }

    public KafkaClient getKafkaConcumerClient() {
        return new KafkaClient.Builder().setTopic(streamTopic).setBrokerAddress(brokerAddress).build();
    }
}
