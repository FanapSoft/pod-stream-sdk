package ir.fanap.podstream.kafka;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ir.fanap.podstream.util.Utils;

public class KafkaConsumer extends KafkaClient {
    private ConsumerClient consumerClient;
    private Properties properties;
    private List<String> activeKeys;

    public KafkaConsumer(KafkaConsumer.Builder builder) {
        super(builder);
        this.properties = builder.properties;
        this.activeKeys = new ArrayList();
        this.consumerClient = new ConsumerClient(this.getProperties(), this.getTopic());
    }

    public Properties getProperties() {
        return this.properties;
    }

    public KafkaConsumer setActiveKeys(List<String> activeKeys) {
        this.activeKeys = activeKeys;
        return this;
    }

    public void addActiveKey(String key) {
        this.activeKeys.add(key);
    }

    public boolean isKeyActive(String key) {
        return this.activeKeys.contains(key);
    }

    public boolean deActiveKey(String key) {
        return this.activeKeys.remove(key);
    }

    public void connect() {
        this.consumerClient.connect();
        this.activate();
    }

    public byte[] consumeMessage(int timeout) {
        return this.consumerClient.consumingTopic(timeout);
    }

    @Nullable
    public ConsumResult consumeMessage(int timeout, String key) {
        ConsumResult cr = consumerClient.consumingWithKey(100);
        return cr;
    }

    public void close() {
        super.close();
        this.consumerClient.close();
    }

    public static class Builder extends KafkaClient.Builder {
        private Properties properties;

        public KafkaConsumer.Builder setBrokerAddress(String brokerAddress) {
            super.setBrokerAddress(brokerAddress);
            return this;
        }

        public KafkaConsumer.Builder setTopic(String topic) {
            super.setTopic(topic);
            return this;
        }

        public Builder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public KafkaConsumer build() {
            return new KafkaConsumer(this);
        }
    }
}
