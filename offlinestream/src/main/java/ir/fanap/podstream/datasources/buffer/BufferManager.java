package ir.fanap.podstream.datasources.buffer;

import java.util.Stack;
import ir.fanap.podstream.datasources.model.VideoPacket;

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


    public byte[] read(long offset, long length) {
        if ((offset + length) > fileSize)
            length = (int) (fileSize - readedPos);
        byte[] result = new byte[(int) length];
        int resultPosition = 0;
        while (true) {
            if (existInBuffer(offset, length)) {
                if (!existInCurrent(offset, length) && !partExistInCurrent(offset)) {
                    changeCurrent();
                    continue;
                }
                if (existInCurrent(offset, length)) {
                    System.arraycopy(current.getBytes(), current.getReaded(), result, resultPosition, (int) length);
                    readedPos += length;
                    current.setReaded((int) length);
                    break;
                } else {
                    long newlength = (current.getEnd() - offset) + 1;
                    length = length - newlength;
                    offset += newlength;
                    System.arraycopy(current.getBytes(), current.getReaded(), result, resultPosition, (int) newlength);
                    resultPosition = (int) (resultPosition + newlength);
                    readedPos += newlength;
                    if (length == 0) {
                        break;
                    }
                    changeCurrent();
                    offset = current.getStart();
                    continue;
                }
            } else {
                resetBuffer((int) offset);
                continue;
            }
        }
        return result;
    }

    public void resetBuffer(int offset) {
        startBuffer = offset;
        endBuffer = endBuffer + 200;
        if (endBuffer > fileSize)
            endBuffer = fileSize;
        current = null;
        buffer.clear();
    }

    public boolean needsUpdate() {
        return true;
    }

    private boolean existInBuffer(long offset, long length) {
        return (buffer != null && offset >= startBuffer && (offset + length) <= endBuffer);
    }

    private boolean existInCurrent(long offset, long length) {
        return (current != null && offset >= current.getStart() && (offset + length) <= current.getEnd());
    }

    private boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    private void changeCurrent() {
        if (buffer.isEmpty())
            return;
        current = buffer.firstElement();
        removefromBufer(0);
        startBuffer = current.getStart();
    }

    public void addToBuffer(VideoPacket packet) {
        bufferedSize = packet.getEnd();
        if (current == null) {
            current = packet;
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
