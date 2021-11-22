package ir.fanap.podstream.kafka;

import android.util.Log;

import com.example.kafkassl.kafkaclient.ConsumResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import ir.fanap.podstream.datasources.BaseListener;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.Utils;
import ir.fanap.podstream.util.ssl.SSLHelper;

public class KafkaClientManager {
    public interface Listener extends BaseListener {
        default void onFileReady(long fileSize) {
        }

        default void onRecivedFileChank(byte[] chank) {
        }

    }

    HashMap<String, Listener> listeners;
    private static KafkaClientManager instance;
    public static final int CUNSUME_TIMEOUT = 100;
    private KafkaConsumer consumer;
    private KafkaProducer producer;
    private SSLHelper sslHelper;
    private String token;
    private final long KAFKA_MEESSAGE_GET_FILE_INFORMATION = -5;
    private final long KAFKA_MEESSAGE_GET_FILE_BYTE = -3;
    private final long KAFKA_MEESSAGE_STOP_STREAMING = -2;
    private boolean isStreaming = false;

    public static KafkaClientManager getInstance(SSLHelper sslHelper) {
        if (instance == null) {
            instance = new KafkaClientManager();
            instance.sslHelper = sslHelper;
            instance.listeners = new HashMap();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setListener(String key, Listener listener) {
        this.listeners.put(key, listener);
    }

    public void removeListener(String key) {
        this.listeners.remove(key);
    }

    public void createProducer(KafkaClient producerClient) {
        this.producer = KafkaFactory.createProducer(producerClient, sslHelper);
    }

    public void createConsumer(KafkaClient producerClient) {
        this.consumer = KafkaFactory.createConsumer(producerClient, sslHelper);
    }

    public void produceFileSizeMessage(String key) {
        if (token == null) {
            handleError("Token is Null");
            return;
        }
        producer.produceMessage(
                ByteBuffer.allocate(Long.BYTES).
                        putLong(KAFKA_MEESSAGE_GET_FILE_INFORMATION).
                        array(), key + "," + token);
    }

    int readPosition;

    public void produceFileChankMessage(String key) {
        if (token == null) {
            handleError("Token is Null");
            return;
        }
        String[] arrOfStr = key.split(",", 2);

        int length = Integer.parseInt(arrOfStr[1]);

        byte[] chank = new byte[length];
        System.arraycopy(buffer, readPosition, chank, 0, length);
        readPosition = readPosition + length;


        handleFileChanckRecived(chank);
        Log.e(PodStream.TAG, "produceFileChankMessage: " + Arrays.toString(arrOfStr));


        if (true)
            return;

        /////////////////////////////////////////////////////////////////////////////// will be remove
        producer.produceMessage(
                ByteBuffer.allocate(Long.BYTES).
                        putLong(KAFKA_MEESSAGE_GET_FILE_BYTE).
                        array(), key + "," + token);
    }

    public void stopConsume() {
        if (consumer.isActive())
            consumer.deActive();
        isStreaming = false;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void consume() {
        prepareFile();
        if (true)
            return;
        if (!consumer.isActive())
            consumer.activate();
        isStreaming = true;

        new PodThreadManager().doThisAndGo(() -> {
            String key = "-1";
            while (consumer.isActive()) {
                ConsumResult cr = consumer.consumeMessage(CUNSUME_TIMEOUT, key);
                key = new String(cr.getKey());

                if (key.equals("10")) {
                    handleError(new String(cr.getValue()));
                }
                if (key.equals("5")) {
                    handleFileSizeRecived(Utils.byteArrayToLong(cr.getValue()));
                }
                if (key.equals(String.valueOf(Constants.DefaultLengthValue))) {
                    Log.e(PodStream.TAG, "consume: " + key);
                    handleFileChanckRecived(cr.getValue());
                }
            }
        });
    }

    public void handleError(String error) {
        for (Listener listener : listeners.values()) {
            listener.onError(1, error);
        }
    }

    byte[] buffer;

    public void prepareFile() {
        readPosition = 0;
        final File root = android.os.Environment.getExternalStorageDirectory();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] videoBuffer = new byte[0];
        File file = new File(root.getAbsolutePath() + "/test2flv.mp4");
        byte[] temp = new byte[(int) file.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(temp);
            buffer = ByteBuffer.allocate(videoBuffer.length + temp.length)
                    .put(videoBuffer)
                    .put(temp)
                    .array();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        handleFileSizeRecived(buffer.length);
    }

    public void handleFileSizeRecived(long fileSize) {
        listeners.get("main").onFileReady(fileSize);
    }

    public void handleFileChanckRecived(byte[] chank) {
        listeners.get("provider").onRecivedFileChank(chank);
    }

    public void connectConsumer() {
        if (this.consumer != null)
            this.consumer.connect();
    }

    public void connectProducer() {
        if (this.producer != null)
            this.producer.connect();
    }

    public void closeProducer() {
        try {
            if (this.producer != null) {
                this.producer.close();
                this.producer = null;
            }
        } catch (Exception var2) {
        }
    }

    public void closeConsumer() {
        try {
            if (this.consumer != null) {
                this.consumer.close();
                this.consumer = null;
            }
        } catch (Exception var2) {
        }
    }

    public byte[] getBuffer() {
        return buffer;
    }
}
