package ir.fanap.podstream.data;

import android.util.Log;

import java.util.Stack;

import ir.fanap.podstream.data.model.Packet;
import ir.fanap.podstream.util.PodThreadManager;

public class BufferManager implements EventListener.KafkaListener {


    EventListener.BufferListener listener;
    KafkaManager kafkaManager;
    Stack<Packet> buffer;
    Packet current;

    int startBuffer, endBuffer;
    int remaning;
    int fileSize;
    int bufferedSize;
    int readedPos;
    boolean isReady = false;

    @Override
    public void fileReady(int filSize) {
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
        endBuffer = 1000;
        readedPos = 0;
    }

    public BufferManager setListener(EventListener.BufferListener listener) {
        this.listener = listener;
        return this;
    }

    public void prepare() {
        kafkaManager = new KafkaManager().setListener(this);
        kafkaManager.prepare();
    }

    @Override
    public void write(Packet packet) {
        addToBuffer(packet);
    }

    @Override
    public void isEnd(boolean isEnd) {
        listener.isEnd(isEnd);
        isReady = false;

    }

    public void getData(int offset, int length) {
        checkBuffer(offset, length);
    }


    private void checkBuffer(int offset, int length) {
        if ((offset + length) > fileSize)
            length = fileSize - readedPos;
        int[] result = new int[length];
        int resultPosition = 0;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isReady)
                break;
            if (existInBuffer(offset, length)) {
                if (!existInCurrent(offset, length) && !partExistInCurrent(offset)) {
                    changeCurrent();
                    continue;
                }
                if (existInCurrent(offset, length)) {
                    System.arraycopy(current.getData(), current.getPackRemainig(), result, resultPosition, length);
                    readedPos += length;
                    current.setPackRemainig(length);
                    break;
                } else {
                    int newlength = (current.getEnd() - offset) + 1;
                    length = length - newlength;
                    offset += newlength;
                    System.arraycopy(current.getData(), current.getPackRemainig(), result, resultPosition, newlength);
                    resultPosition = resultPosition + newlength;
                    readedPos += newlength;
                    if (length == 0) {
                        break;
                    }
                    changeCurrent();
                    offset = current.getStart();
                    continue;
                }
            } else {
                resetBuffer(offset);
                continue;
            }

        }

        ((Runnable) () -> listener.write(result)).run();
    }

    public void resetBuffer(int offset) {
        startBuffer = offset;
        endBuffer = endBuffer + 200;
        if (endBuffer > fileSize)
            endBuffer = fileSize;
        current = null;
        buffer.clear();

    }

    private boolean existInBuffer(int offset, int length) {
        return (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    private boolean existInCurrent(int offset, int length) {
        return (current != null && offset >= current.getStart() && (offset + length) <= current.getEnd());
    }

    private boolean partExistInCurrent(int offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    private void updateBuffer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isReady) {
                    if (buffer.size() < 5) {
                        kafkaManager.read(bufferedSize, 10);
                    }
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

    private void addToBuffer(Packet packet) {
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
}
