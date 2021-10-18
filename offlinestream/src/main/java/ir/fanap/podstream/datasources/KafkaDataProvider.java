package ir.fanap.podstream.datasources;
import android.util.Log;
import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;
import org.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import java.util.Properties;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.TimeOutUtils;
import ir.fanap.podstream.util.Utils;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;
import ir.fanap.podstream.offlinestream.PodStream;

public class KafkaDataProvider {
    public interface Listener {
        void onStreamerIsReady(boolean state);

        void onFileReady(DashResponse dashFile);

        void onTimeOut();

        void onError(String message);
    }

    Listener listener;
    DashResponse dashFile;
    ConsumerClient consumerClient;
    ProducerClient producerClient;
    String consumTopic;
    String produceTopic;
    private byte[] mainBuffer;
    private long offsetMainBuffer;
    private long endOfMainBuffer;
    private long filmLength;
    boolean isTimeOut = false;
    Object timeOutObg = null;

    public Listener getListener() {
        return listener;
    }

    public KafkaDataProvider(TopicResponse kafkaConfigs, Listener listener) {
        timeOutObg = startTimeOutSchedule(Constants.DefaultTimeOut);
        this.listener = listener;
        consumTopic = kafkaConfigs.getStreamTopic();
        produceTopic = kafkaConfigs.getControlTopic();
        final Properties propertiesProducer = Utils.getSslProperties(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath());
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", "264");
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();
        if (listener != null)
            listener.onStreamerIsReady(true);
        cancelTimeOutSchedule(timeOutObg);
    }


    public void prepareDashFileForPlay(String Hash, String Token) {
        timeOutObg = startTimeOutSchedule(Constants.DefaultTimeOut);
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-5);
        producerClient.produceMessege(buffers.array(), Hash + "," + Token, produceTopic);
        String key = "-1";
        while (!key.startsWith("5")) {
            ConsumResult cr = consumerClient.consumingWithKey(1000);
            key = new String(cr.getKey());
            long fileSize = Utils.byteArrayToLong(cr.getValue());
            this.dashFile.setSize(fileSize);
            if (isTimeOut)
                break;
        }
        cancelTimeOutSchedule(timeOutObg);
        if (listener != null) {
            listener.onFileReady(this.dashFile);
        }
    }

    public void startStreming(DashResponse dashFile) {
        timeOutObg = startTimeOutSchedule(Constants.PrepareFileTimeOut);
        this.dashFile = dashFile;
        this.filmLength = dashFile.getSize();
        byte[] startBuffer = null;
        while (startBuffer == null || startBuffer.length < 250000) {
            if (isTimeOut)
                break;
            startBuffer = this.consumerClient.consumingWithKey(20).getValue();
        }
        cancelTimeOutSchedule(timeOutObg);
    }

    public byte[] getDataBuffer() {
        return mainBuffer;
    }

    public long getOffsetMainBuffer() {
        return offsetMainBuffer;
    }

    public boolean shouldUpdateBuffer(long offset, long length) {
        return offset >= endOfMainBuffer || offset < offsetMainBuffer || (offset + length) > endOfMainBuffer;
    }

    public void updateBuffer(long offset, long length) {
        if (length > Constants.DefaultLengthValue) {
            getData(offset, length);
        } else {
            timeOutObg = startTimeOutSchedule(Constants.PrepareFileTimeOut);
            if ((offset + length) > filmLength)
                length = filmLength - offset;
            offsetMainBuffer = offset;
            endOfMainBuffer = offset + (length - 1);
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), offset + "," + length, produceTopic);
            mainBuffer = consumerClient.consumingTopic(5);
            while ((mainBuffer == null || mainBuffer.length < length)) {
                if (isTimeOut)
                    break;
                mainBuffer = consumerClient.consumingTopic(5);
            }
            cancelTimeOutSchedule(timeOutObg);
        }
    }


    // TODO Can be better
    // timeout system can be improve
    private Object startTimeOutSchedule(int delayTime) {
        Log.e(PodStream.TAG, "ping !");
        isTimeOut = false;
        return TimeOutUtils.setTimeout(() -> {
            isTimeOut = true;
            if (listener != null)
                listener.onTimeOut();
        }, delayTime);
    }

    private void cancelTimeOutSchedule(@NotNull Object tid) {
        Log.e(PodStream.TAG, "pong !");
        TimeOutUtils.clearTimeout(tid);
        timeOutObg = null;
    }

    public void getData(long offset, long length) {
        timeOutObg = startTimeOutSchedule(Constants.MaxStremerTimeOut);
        mainBuffer = new byte[(int) length];
        offsetMainBuffer = offset;
        endOfMainBuffer = offset + (length - 1);
        boolean exit = false;
        for (int i = 0; i < length; i += Constants.DefaultLengthValue) {
            int newlength = Constants.DefaultLengthValue;
            if (i + newlength > length) {
                newlength = (int) length - i;
                exit = true;
            }
            byte[] newData;
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), (i + offset) + "," + newlength, produceTopic);
            newData = consumerClient.consumingTopic(5);
            while (newData == null || newData.length < newlength) {
                if (isTimeOut)
                    break;
                newData = consumerClient.consumingTopic(5);
            }
            System.arraycopy(newData, 0, mainBuffer, i, newlength);
            if (exit)
                break;
        }
        cancelTimeOutSchedule(timeOutObg);
    }

    public void release() {
        isTimeOut = true;
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);
        if (timeOutObg != null) {
            cancelTimeOutSchedule(timeOutObg);
        }
    }

    public void stopStreaming() {
        isTimeOut = true;
    }
}
