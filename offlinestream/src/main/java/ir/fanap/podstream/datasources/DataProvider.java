package ir.fanap.podstream.datasources;


import android.util.Log;

import ir.fanap.podstream.datasources.buffer.BufferManager;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.kafka.KafkaClientManager;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.Utils;

public class DataProvider implements KafkaClientManager.Listener {
    interface Listener {
        void reset();
    }

    BufferManager bufferManager;
    KafkaClientManager kafkaManager;
    Listener listener;
    long fileSize;
    boolean isWaitForPacket = false;
    private long readPosition;
    private long lastLength;
    boolean isReady;

    public DataProvider setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public DataProvider(KafkaClientManager clientManager, long fileSize) {
        kafkaManager = clientManager;
        kafkaManager.setListener("provider", this);
        bufferManager = new BufferManager();
        bufferManager.prepareBuffer(fileSize);
        readPosition = 0;
        this.fileSize = fileSize;
        startUpdaterJob();
    }

    private long bufferreadPosition;

    public byte[] read(long offset, long length) {
        if ((offset + length) > fileSize)
            length = (int) (fileSize - bufferreadPosition);
        byte[] result = new byte[(int) length];
        int resultPosition = 0;
        while (true) {
            if (bufferManager.existInBuffer(offset, length)) {
                if (!bufferManager.existInCurrent(offset, length) && !bufferManager.partExistInCurrent(offset)) {
                    changeCurrent();
                    continue;
                }
                if (bufferManager.existInCurrent(offset, length)) {
                    System.arraycopy(bufferManager.getCurrent().getBytes(), (int) (offset-bufferManager.getCurrent().getStart()), result, resultPosition, (int) length);
                    bufferreadPosition += length;
                    bufferManager.getCurrent().setReaded((int) length);
                    break;
                } else {
                    long newlength = (bufferManager.getCurrent().getEnd() - offset) + 1;
                    length = length - newlength;
                    offset += newlength;
                    System.arraycopy(bufferManager.getCurrent().getBytes(), bufferManager.getCurrent().getReaded(), result, resultPosition, (int) newlength);
                    resultPosition = (int) (resultPosition + newlength);
                    bufferreadPosition += newlength;
                    if (length == 0) {
                        break;
                    }
                    changeCurrent();
                    offset = bufferManager.getCurrent().getStart();
                    Utils.showLog("exist in next");
                    continue;
                }
            } else {
//                resetBuffer((int) offset);
                continue;
            }
        }
        return result;
    }


    public void changeCurrent() {
        bufferManager.changeCurrent();
        Utils.showLog("current changed");
    }

    private void startUpdaterJob() {
        isReady = true;
        new Thread(() -> {
            while (isReady) {
//            while (kafkaManager.isStreaming()) {
                if (bufferManager.needsUpdate() && !isWaitForPacket) {
                    isWaitForPacket = true;
                    lastLength = Constants.DefaultLengthValue;
                    if (readPosition + lastLength > fileSize)
                        lastLength = fileSize - readPosition;
                    kafkaManager.produceFileChankMessage(readPosition + "," + lastLength);
                    if (lastLength < Constants.DefaultLengthValue)
                        isReady = false;
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
        bufferManager.addToBuffer(new VideoPacket(chank, readPosition, (readPosition + lastLength) - 1));
        readPosition = (readPosition + lastLength);
        if (readPosition == fileSize)
            isReady = false;
        isWaitForPacket = false;

//        startBuffer = endBuffer + 1;
//        endBuffer = (startBuffer + lastLength);
//
//
//        if (endBuffer == fileSize) {
//            Log.e(PodStream.TAG, "end of file end : " + endBuffer + " end:  " + fileSize);
//            isReady = false;
//            isWaitForPacket = false;
//        } else {
//            buffered = buffered + lastLength;
//            isWaitForPacket = false;
//        }

        Log.e(PodStream.TAG, "onRecivedFileChank: readPosition :" + readPosition + " lastLength:  " + lastLength);
    }
}
