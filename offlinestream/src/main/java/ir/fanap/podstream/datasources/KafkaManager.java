package ir.fanap.podstream.datasources;

import android.util.Log;

import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.entity.FileSetup;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.TimeOutUtils;
import ir.fanap.podstream.util.Utils;

public class KafkaManager {

    boolean isConnect = false;
    long start = 0;
    long end = 2000;
    long fileSize = 0;
    boolean isEndOfFile = false;
    private final long KAFKA_MEESSAGE_GET_FILE_INFORMATION = -5;
    private final long KAFKA_MEESSAGE_GET_FILE_BYTE = -3;
    private final long KAFKA_MEESSAGE_STOP_STREAMING = -2;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    KafkaProcessHandler.ProccessHandler listener;
    String token;
    boolean hasError = false;
    Object timeOutObg = null;

    public KafkaManager setListener(KafkaProcessHandler.ProccessHandler listener) {
        this.listener = listener;
        return this;
    }

    public KafkaManager(KafkaProcessHandler.ProccessHandler listener) {
        this.listener = listener;
    }

    public void produceNextChankMessage(KafkaProcessHandler.ProccessHandler listener) {
        if (!isConnect) {
            listener.onError(0, "Not Ready");
            return;
        }
        if (isEndOfFile) {
            listener.onStreamEnd();
        }
        listener.onFileBytes(new byte[10]);
        start = (start + end) + 1;
        if ((start + end) > fileSize) {
            end = fileSize - start;
            isEndOfFile = true;
        }
    }

    public void changeStartOffset(long start) {
        this.start = start;
    }

    public void produceCloseMessage() {
        produceMessage(KAFKA_MEESSAGE_STOP_STREAMING, ",");
    }

    public void produceFileSizeMessage(String hash, KafkaProcessHandler.ProccessHandler listener) {
        if (!isConnect) {
            listener.onError(0, "Not Ready");
            return;
        }
        timeOutObg = startTimeOutSchedule(10000);

        produceMessage(KAFKA_MEESSAGE_GET_FILE_INFORMATION, hash + "," + token);
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

//
//    public void prepareDashFileForPlay(FileSetup file, String Token) {
//        this.dashFile = new DashResponse();
//        timeOutObg = startTimeOutSchedule(Constants.DefaultTimeOut);
//        sendMessageToKafka(KAFKA_MEESSAGE_GET_FILE_INFORMATION, file.getVideoAddress() + "," + Token);
//        String key = "-1";
//        while (!key.startsWith("5")) {
//            ConsumResult cr = consumerClient.consumingWithKey(100);
//            key = new String(cr.getKey());
//            long fileSize = Utils.byteArrayToLong(cr.getValue());
//            this.dashFile.setSize(fileSize);
//            if (streamerIsStoped)
//                break;
//        }
//        if (file.getVideoAddress().equals("296FF59BVT6M8OLW"))
//            this.dashFile.setSize(147031744);
//
//        cancelTimeOutSchedule(timeOutObg);
//        if (listener != null) {
//            listener.onFileReady(this.dashFile);
//        }
//    }
//
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

    private void disconnect() {
        isConnect = false;
        produceCloseMessage();
    }

    private void reset() {
        start = 0;
        end = 2000;
    }

    // timeout system can be improve
    private Object startTimeOutSchedule(int delayTime) {
        Log.e(PodStream.TAG, "ping !");
        hasError = false;
        return TimeOutUtils.setTimeout(() -> {
            hasError = true;
        }, delayTime);
    }

    private void cancelTimeOutSchedule(@NotNull Object tid) {
        Log.e(PodStream.TAG, "pong !");
        TimeOutUtils.clearTimeout(tid);
        timeOutObg = null;
    }
}
