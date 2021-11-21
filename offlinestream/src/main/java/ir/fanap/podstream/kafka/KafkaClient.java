package ir.fanap.podstream.kafka;

public class KafkaClient {
    private String brokerAddress;
    private String topic;
    private String key;
    private boolean active = false;

    public KafkaClient() {
    }


    KafkaClient(KafkaClient.Builder builder) {
        this.brokerAddress = builder.brokerAddress;
        this.topic = builder.topic;
        this.key = builder.key;
    }

    public String getBrokerAddress() {
        return this.brokerAddress;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getKey() {
        return this.key;
    }


    public void deActive() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

    public void close() {
    }

    public static class Builder {
        private String brokerAddress;
        private String topic;
        private String key;

        public Builder() {
        }

        public KafkaClient.Builder setBrokerAddress(String brokerAddress) {
            this.brokerAddress = brokerAddress;
            return this;
        }

        public KafkaClient.Builder setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public KafkaClient.Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public KafkaClient build() {
            return new KafkaClient(this);
        }
    }
}
