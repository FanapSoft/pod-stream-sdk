//package ir.fanap.podstream.datasources.buffer;
//
//
//import android.util.Log;
//
//import java.util.Stack;
//
//import ir.fanap.podstream.datasources.KafkaManager;
//import ir.fanap.podstream.datasources.customdatasource.KafkaProcessHandler;
//import ir.fanap.podstream.datasources.model.VideoPacket;
//import ir.fanap.podstream.network.response.TopicResponse;
//import ir.fanap.podstream.util.Constants;
//import ir.fanap.podstream.util.PodThreadManager;
//import ir.fanap.podstream.util.ThreadEvent;
//
//public class BufferManager {
//    private long startBuffer = 0;
//    private long endBuffer = Constants.DEAFULT_BUFFER_LENGTH;
//    private Stack<VideoPacket> buffer;
//    private VideoPacket currentPacket;
//    KafkaManager kafkaManager;
//    ThreadEvent threadEvent, updaterEvant;
//    boolean streamIsStarted = false;
//    Thread updaterJob;
//
//    public BufferManager(KafkaManager kafkaManager) {
//        buffer = new Stack<>();
//        this.kafkaManager = kafkaManager;
//        threadEvent = new ThreadEvent();
//        updaterEvant = new ThreadEvent();
//    }
//
//    public boolean existInBuffer(long offset, long length) {
//        return !buffer.empty() && (offset >= startBuffer && (offset + length) <= endBuffer);
//    }
//
//
//    public void startUpdaterJob() {
//        streamIsStarted = true;
//        updaterJob = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (streamIsStarted) {
//                    if (threadEvent.isAwait()) {
//                        if (bufferIsReady()) {
//                            threadEvent.signal();
//                        }
//                    }
//                    if (needsUpdate()) {
//                        getNextChank();
//                        try {
//                            updaterEvant.await();
//                            Log.e("TAG", "run: ");
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });
//        updaterJob.start();
//    }
//
//    public boolean bufferIsReady() {
//        return buffer.size() > 5;
//    }
//
//    public boolean needsUpdate() {
//        return (buffer.size() < 10);
//    }
//
//    public boolean existInCurrent(long offset, long length) {
//        return (currentPacket != null) && (offset >= currentPacket.getStart() && offset + length <= currentPacket.getEnd());
//    }
//
//    public boolean existInCurrentAndNext(long offset, long length) {
//        return (offset >= currentPacket.getStart() && (offset + length) <= (currentPacket.getStart() + currentPacket.getStart()));
//    }
//
//    public void addToBuffer(VideoPacket packet) {
//        if (currentPacket == null) {
//            currentPacket = packet;
//            startBuffer = packet.getStart();
//            return;
//        }
//        buffer.push(packet);
//        Log.e("buffer", "endBuffer  " + endBuffer);
//    }
//
//    public VideoPacket getCurrentPacket() {
//        return currentPacket;
//    }
//
//    public long getNextOffset() {
//        return currentPacket.getStart() + currentPacket.getEnd();
//    }
//
//    public void changeCurrentPacket() {
//        if (buffer.empty())
//            return;
//        currentPacket = buffer.firstElement();
//        startBuffer = currentPacket.getStart();
//        buffer.remove(0);
//    }
//
//    public void resetBuffer(long offset) {
//        kafkaManager.changeStartOffset(startBuffer);
//        buffer.clear();
//        startBuffer = offset;
//        endBuffer = offset + Constants.DEAFULT_BUFFER_LENGTH;
//        currentPacket = null;
//        try {
//            threadEvent.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void getNextChank() {
//        new PodThreadManager().doThisAndGo(() -> kafkaManager.produceNextChankMessage(new KafkaProcessHandler.ProccessHandler() {
//            @Override
//            public void onFileBytes(byte[] bytes, long start, long end) {
//                VideoPacket packet = new VideoPacket(bytes, start, end);
//                addToBuffer(packet);
//                updaterEvant.signal();
//            }
//
//            @Override
//            public void onStreamEnd() { }
//
//            @Override
//            public void onError(int code, String message) { }
//        }));
//    }
//
//
//    public void release() {
//        streamIsStarted = false;
//        threadEvent.signal();
//        updaterEvant.signal();
//        threadEvent = null;
//        updaterEvant = null;
//
//
//    }
//
//}
