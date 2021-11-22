package ir.fanap.podstream.datasources.buffer;

import android.util.Log;

import java.util.Random;
import java.util.Stack;

import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.offlinestream.PodStream;

public class BufferManager {

    Stack<VideoPacket> buffer;
    VideoPacket current;
    long startBuffer, endBuffer;
    long remaning;
    long fileSize;
    long bufferedSize;
    long readedPos;


    public void prepareBuffer(long fileSize) {
        this.fileSize = fileSize;
        remaning = fileSize;
        bufferedSize = 0;
        initBuffer();
    }

    private void initBuffer() {
        buffer = new Stack<>();
        startBuffer = 0;
        endBuffer = fileSize;
        readedPos = 0;
    }


    public VideoPacket getCurrent() {
        return current;
    }

    public void resetBuffer(int startBuffer, int endBuffer) {
        this.startBuffer = startBuffer;
        this.endBuffer = endBuffer;
        current = null;
        buffer.clear();
    }

    public boolean needsUpdate() {
        return true;
    }

    public boolean existInBuffer(long offset, long length) {
        return (buffer != null && offset >= startBuffer && (offset + length) <= endBuffer);
    }

    public boolean existInCurrent(long offset, long length) {
        return (current != null && offset >= current.getStart() && (offset + length) <= current.getEnd());
    }

    public boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    public void changeCurrent() {
        if (buffer.isEmpty())
            return;
        current = buffer.firstElement();
//        current.setReaded(1);
        removefromBufer(0);
        startBuffer = current.getStart();
    }

    public byte getRandomLength() {
        Random rand = new Random();
        int min = 0;
        int max = 9;
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return (byte) randomNum;
//        return randomNum;
    }

    public void addToBuffer(VideoPacket packet) {
        bufferedSize = packet.getEnd();
        if (current == null) {
            current = packet;
//            byte[] bu = new byte[5000];
//            for (int i = 0; i < 5000; i++)
//                bu[i] = getRandomLength();
//            current.setBytes(bu);
            return;
        }
        buffer.push(packet);
    }

    private void removefromBufer(int index) {
        buffer.remove(index);
    }


    public void release() {

    }
}
