package ir.fanap.podstream.datasources.buffer;

import java.util.Stack;

import ir.fanap.podstream.datasources.KafkaManager;
import ir.fanap.podstream.datasources.model.VideoPacket;

public class BufferManager implements EventListener.KafkaListener {


    EventListener.BufferListener listener;
    KafkaManager kafkaManager;
    Stack<VideoPacket> buffer;
    VideoPacket current;

    long startBuffer, endBuffer;
    long remaning;
    long fileSize;
    long bufferedSize;
    long readedPos;
    boolean isReady = false;

    public BufferManager(KafkaManager kafkaManager) {
        this.kafkaManager = kafkaManager;
        this.kafkaManager.setListener(this);
    }

    @Override
    public void onFileReady(long filSize) {
        fileSize = filSize;
        remaning = filSize;
        bufferedSize = 0;
        isReady = true;
        initBuffer();
        updateBuffer();
        ((Runnable) () -> {
            listener.fileReady(filSize);
        }).run();

    }

    private void initBuffer() {
        buffer = new Stack<>();
        startBuffer = 0;
        endBuffer = fileSize;
        readedPos = 0;
    }

    public BufferManager setListener(EventListener.BufferListener listener) {
        this.listener = listener;
        return this;
    }


    @Override
    public void write(VideoPacket packet) {
        addToBuffer(packet);
    }

    @Override
    public void isEnd(boolean isEnd) {
        listener.isEnd(isEnd);
        isReady = false;

    }

    public byte[] getData(long offset, long length) {
        return checkBuffer(offset, length);
    }


    private byte[] checkBuffer(long offset, long length) {
        if ((offset + length) > fileSize)
            length = (int) (fileSize - readedPos);
        byte[] result = new byte[(int) length];
        int resultPosition = 0;
        while (true) {
            if (!isReady)
                break;
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

//        ((Runnable) () -> listener.write(result)).run();
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

    private boolean existInBuffer(long offset, long length) {
        return (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    private boolean existInCurrent(long offset, long length) {
        return (current != null && offset >= current.getStart() && (offset + length) <= current.getEnd());
    }

    private boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    private void updateBuffer() {
        new Thread(() -> {
            while (isReady) {
                if (buffer.size() < 5) {
                    kafkaManager.read(bufferedSize, 10);
                }
            }
        }).start();
    }

    private void changeCurrent() {
        if (buffer.isEmpty())
            return;
        current = buffer.firstElement();
        removefromBufer(0);
        startBuffer = current.getStart();
    }

    private void addToBuffer(VideoPacket packet) {
        bufferedSize = packet.getEnd();
        if (current == null) {
            current = packet;
            return;
        }
        buffer.push(packet);
    }

    @Override
    public void onConnect() {
        listener.onConnect();
    }

    private void removefromBufer(int index) {
        buffer.remove(index);
    }


    public void release() {
//        streamIsStarted = false;
//        threadEvent.signal();
//        updaterEvant.signal();
//        threadEvent = null;
//        updaterEvant = null;
    }
}
