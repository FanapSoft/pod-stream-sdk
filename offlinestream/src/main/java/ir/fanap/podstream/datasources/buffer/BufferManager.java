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
    long countsend = 0;
    long countrecive = 0;

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
        this.endBuffer = (offset + length) - 1;
        current = null;
        Utils.showLog("Masoud Restart buffer :" + offset + " end:" + endBuffer + " length:" + length);
        if (putStack == null) {
            buffer.clear();
            putStack = new PutStack(startBuffer + endBuffer + "", 40, offset, Constants.DefaultLengthValue);
            putStack.start();
        } else {
            putStack.setReaddata(false);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            putStack.restart(offset, Constants.DefaultLengthValue);
        }

    }

    public boolean existInBuffer(long offset, long length) {
        if (offset >= startBuffer && ((offset + length) - 1) <= endBuffer) {
            return true;
        } else {
            Utils.showLog("Masoud Restart buffer Beshavad shoro:" + offset + " payan:" + (offset + length + 1) + " start=" + startBuffer + " endbaffer=" + endBuffer);
            return false;
        }
    }

    public boolean existInCurrent(long offset, long length) {
        if (current != null && offset >= current.getStart() && ((offset + length) - 1) <= current.getEnd()) {
            return true;
        } else {
            Utils.showLog("Masoud Restart current Beshavad shoro:" + offset + " payan:" + (offset + length + 1) + " start=" + current.getStart() + " end=" + current.getEnd() + " len=" + (current.getStart() - current.getEnd()));
            return false;
        }
    }

    public boolean partExistInCurrent(long offset) {
        return (current != null && offset >= current.getStart() && offset <= current.getEnd());
    }

    public boolean checkEmpty() {
//        return (current == null);
//        synchronized (current){
        return (current == null);
//        }
    }

    public void changeCurrent() {
        Utils.showLog("Masoudd current is change oldstartbufer: " + startBuffer + "  oldendbufer:" + endBuffer);
        if(buffer.isEmpty())
            return;
//        while (buffer.isEmpty()) {
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        current = buffer.firstElement();
        removefromBufer(0);
        Utils.showLog("Masoudd delete to buffer -  size :" + buffer.size());
        startBuffer = current.getStart();
//        if(endBuffer < startBuffer+Constants.DefaultLengthValue){
//            if(startBuffer+Constants.DefaultLengthValue<=fileSize)
//                endBuffer=(startBuffer+Constants.DefaultLengthValue-1);
//            else
//                endBuffer+=((fileSize-startBuffer)-1);
//        }
        if (endBuffer + Constants.DefaultLengthValue <= fileSize)
            endBuffer = (endBuffer + Constants.DefaultLengthValue - 1);
        else
            endBuffer = fileSize - 1;
        Utils.showLog("Masoud current is change newstartbufer: " + startBuffer + "  newendbufer:" + endBuffer);
        Utils.showLog("Masoud buffer size :" + buffer.size());
    }

    public void addToBuffer(VideoPacket packet) {
//        if (packet.getEnd() > endBuffer) {
//            Utils.showLog("Masoud Buffer is grow oldendbufer:" + endBuffer + " newend: " + packet.getEnd());
//            endBuffer = packet.getEnd();
//        }
        if (current == null) {
            current = packet;
            return;
        }
//        synchronized (buffer) {
        buffer.push(packet);
        Utils.showLog("Masoudd insert to buffer -  size :" + buffer.size());
//        }

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
        long startpos;
        long readtol;
        boolean isWaitForPacket;
        int counterwait = 0;
        Object lock;

        public void restart(long readPosition, long readLength) {
            this.readPosition = readPosition;
            this.readLength = readLength;
            this.startpos = readPosition;
            this.readtol = readLength;
            Utils.showLog("Masoud Data of thread restart");
        }

        public PutStack(@NonNull String name, int chankSize, int readPosition, long readLength) {
            super(name);
            this.chankSize = chankSize;
            this.readPosition = readPosition;
            this.readLength = readLength;
            this.startpos = readPosition;
            this.readtol = readLength;
            kafkaManager.setListener("buffer", this);

        }

        long last;

        @Override
        public void run() {
            while (loop && kafkaManager.isStreaming()) {
//                synchronized (lock){
//                if (counterwait > 400)
//                    continue;
//                else
                if (readdata == false) {
                    if (isWaitForPacket)
                        continue;
                    Utils.showLog("Masoud readdata=false and buffer realese: ");
//                        buffer.empty();
                    buffer.clear();
                    readdata = true;
                    continue;
                } else if (startpos <= fileSize) {
//                    Utils.showLog("Masoud start recive packet: ");
                    isWaitForPacket = true;
                    for (int i = 0; (i < (20 - counterwait) && readdata == true); i++) {

                        boolean isbreak = false;
                        if (startpos + readtol > fileSize) {
                            readtol = fileSize - readPosition;
                            isbreak = true;
                        }
                        kafkaManager.produceFileChankMessage(startpos + "," + readtol);
                        Utils.showLog("Masoudd send request kafka position: " + startpos + "  len: " + readtol + " countsend:" + (++countsend));
                        counterwait++;
                        startpos = startpos + readtol;

                        if (isbreak)
                            break;
                        last = System.currentTimeMillis();
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Utils.showLog("send req : ");
                }


//                }
            }
        }


        @Override
        public void onRecivedFileChank(byte[] chank) {
//            synchronized (lock){
            counterwait--;
            if (readdata) {
                Utils.showLog("Masoudd recive packet diff: " + (++countrecive));
                addToBuffer(new VideoPacket(chank, readPosition, (readPosition + readLength) - 1));
//                Log.e(PodStream.TAG, "send recived kafka : readPosition :" + readPosition + " lastLength:  " + readLength);
                readPosition = (readPosition + readLength);
            } else {
                Utils.showLog("Masoud recive packet faile ");
            }
            if (counterwait == 0)
                isWaitForPacket = false;
//            }
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
