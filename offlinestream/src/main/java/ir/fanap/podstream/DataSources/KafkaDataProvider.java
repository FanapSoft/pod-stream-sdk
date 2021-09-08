package ir.fanap.podstream.DataSources;
import android.os.Build;
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
        this.listener = listener;
        consumTopic = kafkaConfigs.getStreamTopic();
        produceTopic = kafkaConfigs.getControlTopic();
      //  final Properties propertiesProducer = Utils.getSslProperties(kafkaConfigs.getBrokerAddress(), kafkaConfigs.getSslPath());
        final Properties propertiesProducer = new Properties();
        propertiesProducer.setProperty("bootstrap.servers", kafkaConfigs.getBrokerAddress());
        producerClient = new ProducerClient(propertiesProducer);
        producerClient.connect();
        propertiesProducer.setProperty("group.id", String.valueOf(System.currentTimeMillis()));
        propertiesProducer.setProperty("auto.offset.reset", "beginning");
        consumerClient = new ConsumerClient(propertiesProducer, consumTopic);
        consumerClient.connect();

        producerClient.produceMessege("salam".getBytes(),"test","test");
        producerClient.produceMessege("salam".getBytes(),"test","test");
        producerClient.produceMessege("salam".getBytes(),"test","test");
        if (listener != null)
            listener.onStreamerIsReady(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void prepareDashFileForPlay(String Hash, String Token) {
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
        if (listener != null) {
            listener.onFileReady(this.dashFile);
        }
    }

    public void startStreming(DashResponse dashFile) {
        this.dashFile = dashFile;
        this.filmLength = dashFile.getSize();
        byte[] startBuffer = null;
        while (startBuffer == null || startBuffer.length < 250000) {
            startBuffer = this.consumerClient.consumingWithKey(100).getValue();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateBuffer(long offset, long length) {
        if (length > Constants.DefaultLengthValue) {
            getData(offset, length);
        } else {
            if ((offset + length) > filmLength)
                length = filmLength - offset;
            offsetMainBuffer = offset;
            endOfMainBuffer = offset + (length - 1);
            ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
            buffers.putLong(-3);
            producerClient.produceMessege(buffers.array(), offset + "," + length, produceTopic);
            mainBuffer = consumerClient.consumingTopic(5);
            while (mainBuffer == null || mainBuffer.length < length) {
                mainBuffer = consumerClient.consumingTopic(5);
            }
        }
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
            while (newData == null || newData.length < newlength) {
                newData = consumerClient.consumingTopic(5);
            }
            System.arraycopy(newData, 0, mainBuffer, i, newlength);
            if (exit)
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void release() {
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        buffers.putLong(-2);
        producerClient.produceMessege(buffers.array(), ",", produceTopic);
    }
}
