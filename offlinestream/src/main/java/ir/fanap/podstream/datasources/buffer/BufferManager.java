package ir.fanap.podstream.datasources.buffer;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Stack;

import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.kafka.KafkaClientManager;
import ir.fanap.podstream.offlinestream.PodStream;
import ir.fanap.podstream.util.Constants;
import ir.fanap.podstream.util.Utils;


public class BufferManager {

    Stack<VideoPacket> buffer;
    VideoPacket current;
    long startBuffer, endBuffer;
    long remaning;
    long fileSize;

    KafkaClientManager kafkaManager;
    PutStack putStack;

    public void prepareBuffer(long fileSize) {
        this.fileSize = fileSize;
        remaning = fileSize;
        initBuffer();
    }

    public BufferManager() {
        kafkaManager = KafkaClientManager.getInstance(null);
    }

    private void initBuffer() {
        buffer = new Stack<>();
        startBuffer = 0;
        endBuffer = 0;
    }


    public VideoPacket getCurrent() {
        return current;
    }

    public long getStartBuffer() {
        return startBuffer;
    }

    public long getEndBuffer() {
        return endBuffer;
    }

    public void resetBuffer(int offset, int length) {
        if (putStack != null) {
            putStack.stopUpdate();
            putStack.interrupt();
            putStack = null;
        }
//        Utils.showLog("send before reset : offset :" + offset + ",length : " + (offset + length) + " start buffer : " + startBuffer + " end buffer :" + endBuffer);
        this.startBuffer = offset;
        this.endBuffer = offset + length;
        current = null;
        buffer.clear();
//        Utils.showLog("send reset buffer :" +offset + "end :" + endBuffer);
        putStack = new PutStack(String.valueOf((System.currentTimeMillis() / 100)), 10, offset, Constants.DefaultLengthValue);
    }

    public boolean existInBuffer(long offset, long length) {
        return (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    public boolean existInCurrent(long offset, long length) {
        return (current != null && offset >= current.getStart() && (offset + length) <= current.getEnd());
    }

    public boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    public boolean checkEmpty() {
//        return (current == null);
        return (current == null | buffer == null || buffer.size() < 1);
    }

    public void changeCurrent() {
        if (buffer.isEmpty()) {
//            current = null; ???
            return;
        }
        Utils.showLog("current start :" + current.getStart() + "current end : " + current.getEnd());
        current = buffer.firstElement();
        removefromBufer(0);
        startBuffer = current.getStart();
        Utils.showLog("send  buffer size :" + buffer.size());
    }

    public void addToBuffer(VideoPacket packet) {
        if (packet.getEnd() > endBuffer) {
            Utils.showLog("send end buffer epdated : start :" + packet.getStart() + ", end  :" + packet.getEnd());
            endBuffer = packet.getEnd();
        }
        if (current == null) {
            current = packet;
            return;
        }
        buffer.push(packet);
        Utils.showLog("buffer size :" + buffer.size());
    }

    private void removefromBufer(int index) {
        buffer.remove(index);
    }

    public void release() {

    }

    public class PutStack extends Thread implements KafkaClientManager.Listener {

        boolean loop = true;
        int chankSize = 5;
        long readPosition;
        long readLength;
        boolean isWaitForPacket;

        public PutStack(@NonNull String name, int chankSize, int readPosition, long readLength) {
            super(name);
            this.chankSize = chankSize;
            this.readPosition = readPosition;
            this.readLength = readLength;
            kafkaManager.setListener("buffer", this);
            start();
        }

        long last;

        @Override
        public void run() {
            while (loop && kafkaManager.isStreaming()) {
                if (buffer.size() < chankSize && !isWaitForPacket && readPosition < fileSize) {
                    isWaitForPacket = true;
                    if (readPosition + readLength > fileSize)
                        readLength = fileSize - readPosition;

//                    Utils.showLog("send kafka : " + readPosition + "," + readLength);
//                    if ((readPosition + readLength) > endBuffer) {
//                        endBuffer = (readPosition + readLength);
//                        Utils.showLog("send change : " +"startbuffer : " + startBuffer + "endbuffer: " + endBuffer);
//                    }
                    kafkaManager.produceFileChankMessage(readPosition + "," + readLength);
                    last = System.currentTimeMillis();
                    Utils.showLog("send req : ");
                }
            }
        }


        @Override
        public void onRecivedFileChank(byte[] chank) {

            Utils.LogWithDiff("send req : ", last);
            addToBuffer(new VideoPacket(chank, readPosition, (readPosition + readLength) - 1));

            Log.e(PodStream.TAG, "send recived kafka : readPosition :" + readPosition + " lastLength:  " + readLength);
            readPosition = (readPosition + readLength);
            isWaitForPacket = false;
        }


        public void stopUpdate() {
            loop = false;
        }

    }

//

}
