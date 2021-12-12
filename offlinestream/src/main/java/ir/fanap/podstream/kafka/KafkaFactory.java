package ir.fanap.podstream.kafka;

import java.util.Properties;

import ir.fanap.podstream.util.ssl.SSLHelper;

public class KafkaFactory {

    public static KafkaConsumer createConsumer(KafkaClient client,SSLHelper sslHelper) {
        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("bootstrap.servers", client.getBrokerAddress());
        consumerProperties.setProperty("security.protocol", "sasl_plaintext");
        consumerProperties.setProperty("acks", "0");
        consumerProperties.setProperty("sasl.mechanisms", "PLAIN");
        consumerProperties.setProperty("sasl.username", "admin");
        consumerProperties.setProperty("sasl.password", "admin-secret");

//        consumerProperties.setProperty("ssl.ca.location", sslHelper.getCart().getAbsolutePath());
//        consumerProperties.setProperty("ssl.key.password", "masoud68");

        consumerProperties.setProperty("group.id",System.currentTimeMillis()+"");
        consumerProperties.setProperty("auto.offset.reset", "beginning");
        return (new KafkaConsumer.Builder()).setProperties(consumerProperties).setBrokerAddress(client.getBrokerAddress()).setTopic(client.getTopic()).build();

    }

    public static KafkaProducer createProducer(KafkaClient client, SSLHelper sslHelper) {
        Properties producerProperties = new Properties();
        producerProperties.setProperty("bootstrap.servers", client.getBrokerAddress());
        producerProperties.setProperty("acks", "0");
        producerProperties.setProperty("security.protocol", "sasl_plaintext");
        producerProperties.setProperty("sasl.mechanisms", "PLAIN");
        producerProperties.setProperty("sasl.username", "admin");
        producerProperties.setProperty("sasl.password", "admin-secret");
//
//        producerProperties.setProperty("ssl.ca.location", sslHelper.getCart().getAbsolutePath());
//        producerProperties.setProperty("ssl.key.password", "masoud68");
        return (new KafkaProducer.Builder()).setProperties(producerProperties).setBrokerAddress(client.getBrokerAddress()).setTopic(client.getTopic()).setKey(client.getKey()).build();
    }
}
