package ir.fanap.podstream.datasources.model;

public class VideoPacket {
    byte[] bytes;
    long offset;
    long length;

    public VideoPacket(byte[] bytes, long offset, long length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }


}
