package ir.fanap.podstream.datasources.model;

public class VideoPacket {
    byte[] bytes;
    long start;
    long end;
    int readed;

    public VideoPacket(byte[] bytes, long start, long end) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setReaded(int readed) {
        this.readed = this.readed + readed;
    }

    public int getReaded() {
        return readed;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
