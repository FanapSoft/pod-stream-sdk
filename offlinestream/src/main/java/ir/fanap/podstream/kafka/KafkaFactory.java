package ir.fanap.podstream.kafka;

import java.util.Properties;

import ir.fanap.podstream.util.ssl.SSLHelper;

public class KafkaFactory {

    public static KafkaConsumer createConsumer(KafkaClient client,SSLHelper sslHelper) {
        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("bootstrap.servers", client.getBrokerAddress());
        consumerProperties.setProperty("security.protocol", "SASL_SSL");
        consumerProperties.setProperty("sasl.mechanisms", "PLAIN");
        consumerProperties.setProperty("sasl.username", "rrrr");
        consumerProperties.setProperty("sasl.password", "rrrr");
        consumerProperties.setProperty("ssl.ca.location", sslHelper.getCart().getAbsolutePath());
        consumerProperties.setProperty("ssl.key.password", "masoud68");

        consumerProperties.setProperty("group.id", "264");
        consumerProperties.setProperty("auto.offset.reset", "beginning");
        return (new KafkaConsumer.Builder()).setProperties(consumerProperties).setBrokerAddress(client.getBrokerAddress()).setTopic(client.getTopic()).build();

    }

    public static KafkaProducer createProducer(KafkaClient client, SSLHelper sslHelper) {
        Properties producerProperties = new Properties();
        producerProperties.setProperty("bootstrap.servers", client.getBrokerAddress());
        producerProperties.setProperty("security.protocol", "SASL_SSL");
        producerProperties.setProperty("sasl.mechanisms", "PLAIN");
        producerProperties.setProperty("sasl.username", "rrrr");
        producerProperties.setProperty("sasl.password", "rrrr");
        producerProperties.setProperty("ssl.ca.location", sslHelper.getCart().getAbsolutePath());
        producerProperties.setProperty("ssl.key.password", "masoud68");
        return (new KafkaProducer.Builder()).setProperties(producerProperties).setBrokerAddress(client.getBrokerAddress()).setTopic(client.getTopic()).setKey(client.getKey()).build();
    }
}
