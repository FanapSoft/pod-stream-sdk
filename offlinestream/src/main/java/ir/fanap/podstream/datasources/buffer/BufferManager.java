package ir.fanap.podstream.datasources.buffer;


import java.util.Stack;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.util.Constants;

public class BufferManager {
    private long startBuffer = 0;
    private long endBuffer = Constants.DEAFULT_BUFFER_LENGTH;
    private VideoPacket lastPacket;
    private Stack<VideoPacket> buffer;
    private VideoPacket currentPacket;

    public BufferManager() {
        buffer = new Stack<>();
    }

    public boolean existInBuffer(long offset, long length) {
        return (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    public boolean needsUpdate() {
        return (buffer.size() <= 10);
    }

    public boolean existInCurrent(long offset, long length) {
        return (offset >= currentPacket.getStart() && (offset + length) <= (currentPacket.getStart() + currentPacket.getStart()));
    }

    public boolean existInCurrentAndNext(long offset, long length) {
        return (offset >= currentPacket.getStart() && (offset + length) <= (currentPacket.getStart() + currentPacket.getStart()));
    }

    public void addToBuffer(VideoPacket packet) {
        lastPacket = packet;
        buffer.push(packet);
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
        currentPacket = buffer.pop();
    }

    public void resetBuffer(long offset, long length) {
        buffer.clear();
        startBuffer = offset;
        endBuffer = Constants.DEAFULT_BUFFER_LENGTH;
        currentPacket = null;
    }
}
