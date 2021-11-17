package ir.fanap.podstream.datasources.buffer;


import android.util.Log;

import java.util.Stack;

import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.util.Constants;

public class BufferManager {
    private long startBuffer = 0;
    private long endBuffer = 0;
    private Stack<VideoPacket> buffer;
    private VideoPacket currentPacket;

    public BufferManager() {
        buffer = new Stack<>();
    }

    public boolean existInBuffer(long offset, long length) {
        return !buffer.empty() && (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    public boolean needsUpdate() {
        return (buffer.size() < 10);
    }

    public boolean existInCurrent(long offset, long length) {
        return (currentPacket != null) && (offset >= currentPacket.getStart() && offset + length <= currentPacket.getEnd());
    }

    public boolean existInCurrentAndNext(long offset, long length) {
        return (offset >= currentPacket.getStart() && (offset + length) <= (currentPacket.getStart() + currentPacket.getStart()));
    }

    public void addToBuffer(VideoPacket packet) {
        buffer.push(packet);
        endBuffer = packet.getEnd();
        Log.e("buffer", "endBuffer  " + endBuffer);
    }

    public boolean checkEnoughBuffered() {
        return buffer.size() > 5;
    }

    public VideoPacket getCurrentPacket() {
        return currentPacket;
    }

    public long getNextOffset() {
        return currentPacket.getStart() + currentPacket.getEnd();
    }

    public void changeCurrentPacket() {
        if (buffer.empty())
            return;
        currentPacket = buffer.firstElement();
        buffer.remove(0);
//        Log.e("buffer", "change  " + currentPacket.getStart() + "  " + currentPacket.getEnd());
    }

    public void resetBuffer(long offset, long length) {
        buffer.clear();
        startBuffer = offset;
        endBuffer = Constants.DEAFULT_BUFFER_LENGTH;
        currentPacket = null;
    }
}
