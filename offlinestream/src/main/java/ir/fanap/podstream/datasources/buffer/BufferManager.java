package ir.fanap.podstream.datasources.buffer;


import android.util.Log;

import java.util.Stack;

import ir.fanap.podstream.datasources.KafkaManager;
import ir.fanap.podstream.datasources.KafkaProcessHandler;
import ir.fanap.podstream.datasources.model.VideoPacket;
import ir.fanap.podstream.network.response.TopicResponse;
import ir.fanap.podstream.util.Constants;

public class BufferManager extends Thread {
    private long startBuffer = 0;
    private long endBuffer = 0;
    private Stack<VideoPacket> buffer;
    private VideoPacket currentPacket;
    KafkaManager kafkaManager;
    boolean isWaitingForPacket = false;
    boolean streamIsStarted = false;

    public BufferManager(KafkaManager kafkaManager) {
        buffer = new Stack<>();
        this.kafkaManager = kafkaManager;
    }

    public boolean existInBuffer(long offset, long length) {
        return !buffer.empty() && (offset >= startBuffer && (offset + length) <= endBuffer);
    }

    @Override
    public void run() {
        super.run();
        streamIsStarted = true;
        while (streamIsStarted) {
            try {
                if (needsUpdate() && !isWaitingForPacket) {
                    isWaitingForPacket = true;
                    getNextChank();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
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
    }

    public void resetBuffer(long offset, long length) {
        streamIsStarted = false;
        buffer.clear();
        startBuffer = offset;
        endBuffer = 0;
        currentPacket = null;
        run();
    }

    private void getNextChank() {
        kafkaManager.produceNextChankMessage(new KafkaProcessHandler.ProccessHandler() {
            @Override
            public void onFileBytes(byte[] bytes, long start, long end) {
                VideoPacket packet = new VideoPacket(bytes, start, end);
                addToBuffer(packet);
                isWaitingForPacket = false;
            }

            @Override
            public void onStreamEnd() {
                isWaitingForPacket = true;
            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }


}
