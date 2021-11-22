package ir.fanap.podstream.datasources;

import ir.fanap.podstream.datasources.buffer.BufferManager;
import ir.fanap.podstream.util.PodThreadManager;
import ir.fanap.podstream.util.Utils;

public class DataProvider {
    interface Listener {
        void reset();
    }

    BufferManager bufferManager;
    Listener listener;
    long fileSize;
    private long bufferReadPosition;

    public DataProvider setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public DataProvider(long fileSize) {
        bufferManager = new BufferManager();
        bufferManager.prepareBuffer(fileSize);
        this.fileSize = fileSize;
    }


    public byte[] read(long offset, long length) {
        if ((offset + length) > fileSize)
            length = (int) (fileSize - bufferReadPosition);
        byte[] result = new byte[(int) length];
        int resultPosition = 0;
        while (true) {
            if (bufferManager.existInBuffer(offset, length)) {
                if (bufferManager.checkEmpty())
                    continue;
                else if (bufferManager.existInCurrent(offset, length)) {
                    System.arraycopy(bufferManager.getCurrent().getBytes(), (int) (offset - bufferManager.getCurrent().getStart()), result, resultPosition, (int) length);
                    bufferReadPosition += length;
                    bufferManager.getCurrent().setReaded((int) length);
                    break;
                } else if (bufferManager.partExistInCurrent(offset)) {
                    long newlength = (bufferManager.getCurrent().getEnd() - offset) + 1;
                    length = length - newlength;
//                    offset += newlength;
                    System.arraycopy(bufferManager.getCurrent().getBytes(), bufferManager.getCurrent().getReaded(), result, resultPosition, (int) newlength);
                    resultPosition = (int) (resultPosition + newlength);
                    bufferReadPosition += newlength;
                    if (length == 0) {
                        break;
                    }
                    changeCurrent();
                    offset = bufferManager.getCurrent().getStart();
                    continue;
                } else {
                    changeCurrent();
                    continue;
                }
            } else {
//                Utils.showLog("send before reset : Roffset :" + offset + "Rend : " + (offset + length) + " start buffer : " + bufferManager.getStartBuffer() + " end buffer : " + bufferManager.getEndBuffer());

//                bufferManager.resetBuffer((int) offset, (int) (offset + length));
                bufferManager.resetBuffer((int) offset, (int) fileSize);
                continue;
            }
        }
        return result;
    }

    public void changeCurrent() {
        bufferManager.changeCurrent();
    }

    private void endStreaming() {
        bufferManager.release();
    }

}
