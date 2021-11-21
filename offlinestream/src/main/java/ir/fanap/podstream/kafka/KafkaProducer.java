package ir.fanap.podstream.kafka;
import com.example.kafkassl.kafkaclient.ProducerClient;
import java.util.Properties;

public class KafkaProducer extends KafkaClient {
    private ProducerClient producerClient;
    private Properties properties;


    public KafkaProducer(KafkaProducer.Builder builder) {
        super(builder);
        this.properties = builder.properties;
        this.producerClient = new ProducerClient(this.getProperties());
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void connect() {
        this.producerClient.connect();
        this.activate();
    }

    public void produceMessage(byte[] message) {
        this.producerClient.produceMessege(message, this.getKey(), this.getTopic());
    }

    public void produceMessage(byte[] message, String key) {
        this.producerClient.produceMessege(message, key, this.getTopic());
    }

    public void close() {
        super.close();
        this.producerClient.close();
    }

    public static class Builder extends KafkaClient.Builder {
        private Properties properties;

        public Builder() {
        }

        public KafkaProducer.Builder setProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public KafkaProducer.Builder setBrokerAddress(String brokerAddress) {
            super.setBrokerAddress(brokerAddress);
            return this;
        }

        public KafkaProducer.Builder setTopic(String topic) {
            super.setTopic(topic);
            return this;
        }

        public KafkaProducer.Builder setKey(String key) {
            super.setKey(key);
            return this;
        }

        public KafkaProducer build() {
            return new KafkaProducer(this);
        }
    }
}