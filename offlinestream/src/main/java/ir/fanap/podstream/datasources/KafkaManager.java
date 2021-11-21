package ir.fanap.podstream.datasources;


import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.datasources.buffer.EventListener;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.util.TimeOutUtils;
import ir.fanap.podstream.util.Utils;

public class KafkaManager {
    boolean isConnect = false;
    long fileSize = 0;
    boolean isEndOfFile = false;
    private final long KAFKA_MEESSAGE_GET_FILE_INFORMATION = -5;
    private final long KAFKA_MEESSAGE_GET_FILE_BYTE = -3;
    private final long KAFKA_MEESSAGE_STOP_STREAMING = -2;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    String token;
    boolean hasError = false;
    Object timeOutObg = null;

    EventListener.KafkaListener listener;
    byte[] fileBytes;
    int readPosition;

    public KafkaManager setListener(EventListener.KafkaListener listener) {
        this.listener = listener;
        return this;
    }

    public void connect(String brokerAddress, String sslPath, String consumTopic, String produceTopic, String token) {
        this.token = token;
        this.consumTopic = consumTopic;
        this.produceTopic = produceTopic;
        final Properties properties = Utils.getSslProperties(brokerAddress, sslPath);
        connectProducer(properties);
        connectConcumer(properties);
        isConnect = true;
        listener.onConnect();
    }

    public void produceFileSizeMessage(String hash, EventListener.KafkaListener listener) {
        if (!isConnect) {
            listener.onError(0, "Not Ready");
            return;
        }
        timeOutObg = startTimeOutSchedule(10000);

        produceMessage(KAFKA_MEESSAGE_GET_FILE_INFORMATION, hash);
        String key = "-1";
        while (!key.startsWith("5")) {
            ConsumResult cr = consumerClient.consumingWithKey(100);
            key = new String(cr.getKey());
            fileSize = Utils.byteArrayToLong(cr.getValue());
            if (hasError)
                break;
        }

        if (hasError) {
            listener.onError(0, "time out");
        } else
            listener.onFileReady(fileSize);

        cancelTimeOutSchedule(timeOutObg);
    }


    // timeout system can be improve
    private Object startTimeOutSchedule(int delayTime) {
        hasError = false;
        return TimeOutUtils.setTimeout(() -> {
            hasError = true;
        }, delayTime);
    }

    private void cancelTimeOutSchedule(@NotNull Object tid) {
        TimeOutUtils.clearTimeout(tid);
        timeOutObg = null;
    }

    private void connectProducer(Properties properties) {
        producerClient = new ProducerClient(properties);
        producerClient.connect();
    }

    private void connectConcumer(Properties properties) {
        properties.setProperty("group.id", "264");
        properties.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(properties, consumTopic);
        consumerClient.connect();
    }

    public void produceMessage(long message, String key) {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(message);
        producerClient.produceMessege(buffers.array(), key, produceTopic);
    }



    public long getFileSize() {
        fileSize = fileBytes.length;
        return fileSize;
    }

    public void read(long offset, long length) {
        if (offset + length == fileSize) {
            listener.isEnd(true);
        } else if (offset + length > fileSize) {
            {
                listener.isEnd(true);
            }
        }
        byte[] bytes = new byte[(int) length];
        System.arraycopy(fileBytes, readPosition, bytes, 0, (int) length);
        VideoPacket packet = new VideoPacket(bytes,readPosition, (readPosition + length) - 1);
        readPosition = (int) (readPosition + length);
        listener.write(packet);
    }

    public void produceCloseMessage() {
        produceMessage(KAFKA_MEESSAGE_STOP_STREAMING, ",");
    }

    private void disconnect() {
        isConnect = false;
        produceCloseMessage();
    }

}
