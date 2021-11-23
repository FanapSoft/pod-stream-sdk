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
    int readlen=0;

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

//        Utils.showLog("send before reset : offset :" + offset + ",length : " + (offset + length) + " start buffer : " + startBuffer + " end buffer :" + endBuffer);
        this.startBuffer = offset;
        this.endBuffer = (offset + length)-1;
        current = null;
        Utils.showLog("Masoud Restart buffer :" + offset + "end :" + endBuffer + " length "  +length + " file size=" + fileSize);
        if(putStack==null){
            buffer.clear();
            putStack = new PutStack(startBuffer + endBuffer + "", 20, offset,Constants.DefaultLengthValue);
            putStack.start();
        }else{
            putStack.restart(offset,Constants.DefaultLengthValue);
            putStack.setReaddata(false);
        }

    }

    public boolean existInBuffer(long offset, long length) {
        return (offset >= startBuffer && ((offset + length)-1) <= endBuffer);
    }

    public boolean existInCurrent(long offset, long length) {
        return (current != null && offset >= current.getStart() && ((offset + length)-1) <= current.getEnd());
    }

    public boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    public boolean checkEmpty() {
//        return (current == null);
        return (current == null);
    }

    public void changeCurrent() {
        if (buffer.isEmpty()) {
            return;
        }
        Utils.showLog("Masoud current is change oldstartbufer: " +startBuffer+"  oldendbufer:" + endBuffer);
        current = buffer.firstElement();
        removefromBufer(0);
        startBuffer = current.getStart();
        if(endBuffer < startBuffer + startBuffer+Constants.DefaultLengthValue){
            if(startBuffer+Constants.DefaultLengthValue<=fileSize)
                endBuffer=(startBuffer+Constants.DefaultLengthValue-1);
            else
                endBuffer+=((fileSize-startBuffer)-1);
        }
        Utils.showLog("Masoud current is change newstartbufer: " +startBuffer+"  newendbufer:" + endBuffer);
        Utils.showLog("Masoud buffer size :" + buffer.size());
    }

    public void addToBuffer(VideoPacket packet) {
        if (packet.getEnd() > endBuffer) {
            Utils.showLog("Masoud Buffer is grow oldendbufer:" + endBuffer + " newend: " + packet.getEnd());
            endBuffer = packet.getEnd();
        }
        if (current == null) {
            current = packet;
            return;
        }
        synchronized (buffer){
            buffer.push(packet);
            Utils.showLog("Masoud insert to buffer -  size :" + buffer.size());
        }

    }

    private void removefromBufer(int index) {
        buffer.remove(index);
    }

    public void release() {

    }

    public class PutStack extends Thread implements KafkaClientManager.Listener {

        boolean loop = true;
        boolean readdata = true;
        int chankSize = 5;
        long readPosition;
        long readLength;
        boolean isWaitForPacket;

        public void restart(long readPosition,long readLength){
            this.readPosition=readPosition;
            this.readLength=readLength;
            Utils.showLog("Masoud Data of thread restart");
        }

        public PutStack(@NonNull String name, int chankSize, int readPosition, long readLength) {
            super(name);
            this.chankSize = chankSize;
            this.readPosition = readPosition;
            this.readLength = readLength;
            kafkaManager.setListener("buffer", this);

        }

        long last;

        @Override
        public void run() {
            while (loop && kafkaManager.isStreaming()) {
                if(isWaitForPacket)
                    continue;
                else if(readdata==false){
                    Utils.showLog("Masoud readdata=false and buffer realese: ");
                    buffer.empty();
                    readdata=true;
                    continue;
                }
                else if (buffer.size() < chankSize && readPosition < fileSize) {
                    Utils.showLog("Masoud start recive packet: ");
                    isWaitForPacket = true;
                    if (readPosition + readLength > fileSize)
                        readLength = fileSize - readPosition;
                    Utils.showLog("Masoud send request kafka position: " + readPosition + "  len: " + readLength);
                    kafkaManager.produceFileChankMessage(readPosition + "," + readLength);
                    last = System.currentTimeMillis();
                    Utils.showLog("send req : ");
                }
            }
        }


        @Override
        public void onRecivedFileChank(byte[] chank) {
            if(readdata) {
                Utils.showLog("Masoud recive packet diff: " + (System.currentTimeMillis() - last));
                addToBuffer(new VideoPacket(chank, readPosition, (readPosition + readLength) - 1));
                Log.e(PodStream.TAG, "send recived kafka : readPosition :" + readPosition + " lastLength:  " + readLength);
                readPosition = (readPosition + readLength);
            }
            else{
                Utils.showLog("Masoud recive faile ");
            }
            isWaitForPacket = false;
        }


        public void stopUpdate() {
            loop = false;
        }

        public void setReaddata(boolean readdata) {
            this.readdata = readdata;
        }
    }

//

}
