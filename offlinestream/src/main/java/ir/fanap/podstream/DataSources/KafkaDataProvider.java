package ir.fanap.podstream.DataSources;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.kafkassl.kafkaclient.ConsumResult;
import com.example.kafkassl.kafkaclient.ConsumerClient;
import com.example.kafkassl.kafkaclient.ProducerClient;

import java.nio.ByteBuffer;
import java.util.Properties;

import ir.fanap.podstream.Util.Constants;
import ir.fanap.podstream.Util.Utils;
import ir.fanap.podstream.network.response.DashResponse;
import ir.fanap.podstream.network.response.TopicResponse;

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

    public KafkaDataProvider(TopicResponse kafkaConfigs, Listener listener) {
        Log.e("TimeOutTest", "KafkaDataProvider: ");
        startTimeOutSchedule(Constants.DefaultTimeOut);
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

        cancelTimeOutSchedule();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void prepareDashFileForPlay(String Hash, String Token) {

        startTimeOutSchedule(Constants.DefaultTimeOut);
        Log.e("TimeOutTest", "prepareDashFileForPlay: ");
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-5);
        producerClient.produceMessege(buffers.array(), Hash + "," + Token, produceTopic);
        String key = "-1";
        while (!key.startsWith("5")) {
            ConsumResult cr = consumerClient.consumingWithKey(1000);
            key = new String(cr.getKey());
            long fileSize = Utils.byteArrayToLong(cr.getValue());
            this.dashFile.setSize(fileSize);
        }

        cancelTimeOutSchedule();

        if (listener != null) {
            listener.onFileReady(this.dashFile);
        }
    }

    public void startStreming(DashResponse dashFile) {
        Log.e("TimeOutTest", "startStreming: ");
        startTimeOutSchedule(Constants.PrepareFileTimeOut);

        this.dashFile = dashFile;
        this.filmLength = dashFile.getSize();
        byte[] startBuffer = null;
        while (startBuffer == null || startBuffer.length < 250000 || !isTimeOut) {
            startBuffer = this.consumerClient.consumingWithKey(100).getValue();
        }

        cancelTimeOutSchedule();
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

    boolean isTimeOut = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateBuffer(long offset, long length) {
        if (length > Constants.DefaultLengthValue) {
            Log.e("TimeOutTest", "getData");
            startTimeOutSchedule(Constants.MaxStremerTimeOut);
            getData(offset, length);
        } else {
            Log.e("TimeOutTest", "updateBuffer");
            startTimeOutSchedule(Constants.PrepareFileTimeOut);
            if ((offset + length) > filmLength)
                length = filmLength - offset;
            offsetMainBuffer = offset;
            endOfMainBuffer = offset + (length - 1);
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), offset + "," + length, produceTopic);
            mainBuffer = consumerClient.consumingTopic(5);
            while ((mainBuffer == null || mainBuffer.length < length || !isTimeOut)) {
                mainBuffer = consumerClient.consumingTopic(5);
            }
            cancelTimeOutSchedule();
        }
    }

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isTimeOut = true;
            listener.onTimeOut();
        }
    };

    private void startTimeOutSchedule(int delayTime) {
        Log.e("TimeOutTest", "value  : " + delayTime);
        isTimeOut = false;
        handler.postDelayed(runnable, delayTime);
    }

    private void cancelTimeOutSchedule() {
        handler.removeCallbacks(runnable);
        Log.e("TimeOutTest", "cancel  : ");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getData(long offset, long length) {
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
            while (newData == null || newData.length < newlength || !isTimeOut) {
                newData = consumerClient.consumingTopic(5);
            }
            System.arraycopy(newData, 0, mainBuffer, i, newlength);
            if (exit)
                break;
        }
        cancelTimeOutSchedule();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);
    }
}
