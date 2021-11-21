package ir.fanap.podstream.datasources;


import android.util.Log;

import ir.fanap.podstream.datasources.buffer.BufferManager;
import ir.fanap.podstream.datasources.buffer.EventListener;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.kafka.KafkaClientManager;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.util.Constants;

public class DataProvider implements KafkaClientManager.Listener {
    BufferManager bufferManager;
    KafkaClientManager kafkaManager;
    long fileSize;
    boolean isWaitForPacket = false;
    private long buffered;
    private long startBuffer;
    private long endBuffer;
    private long lastLength;
    boolean isReady;

    public DataProvider(KafkaClientManager clientManager, long fileSize) {
        kafkaManager = clientManager;
        kafkaManager.setListener("provider", this);
        bufferManager = new BufferManager();
        bufferManager.prepareBuffer(fileSize);
        buffered = 0;
        this.fileSize = fileSize;
        startUpdaterJob();
    }

    byte[] read(long offset, long length) {
        while (isReady) ;
        return bufferManager.read(offset, length);
    }

    private void startUpdaterJob() {
        isReady = true;
        new Thread(() -> {
            while (isReady) {
//            while (kafkaManager.isStreaming()) {
                if (bufferManager.needsUpdate() && !isWaitForPacket) {
                    isWaitForPacket = true;
                    Log.e("PodStream", "startUpdaterJob: ");
                    lastLength = Constants.DefaultLengthValue;
                    if (buffered + lastLength > fileSize) {
                        lastLength = fileSize - buffered;
                    }
                    kafkaManager.produceFileChankMessage(startBuffer + "," + lastLength);
                    endBuffer = endBuffer + lastLength;
                    startBuffer = (endBuffer - lastLength);
                }
            }
            endStreaming();
        }).start();
    }

    private void endStreaming() {
        bufferManager.release();
    }

    @Override
    public void onRecivedFileChank(byte[] chank) {
        bufferManager.addToBuffer(new VideoPacket(chank, startBuffer, startBuffer + lastLength));

        if (endBuffer == fileSize) {
            Log.e(PodStream.TAG, "end of file end : " + endBuffer + " end:  " + fileSize);
            isReady = false;
            isWaitForPacket = false;
        } else {
            buffered = buffered + lastLength;
            isWaitForPacket = false;
        }

        Log.e(PodStream.TAG, "onRecivedFileChank: start :" + startBuffer + " end:  " + endBuffer);
    }
}
