package ir.fanap.podstream.data.model;

public class Packet {
    int start;
    int end;
    int[] data;
    int packRemainig;

    public Packet(int start, int end, int[] data) {
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public int getStart() {
        return start;
    }

    public Packet setStart(int start) {
        this.start = start;
        return this;
    }

    public int getEnd() {
        return end;
    }

    public int[] getData() {
        return data;
    }

    public int getPackRemainig() {
        return packRemainig;
    }

    public void setPackRemainig(int packRemainig) {
        this.packRemainig = this.packRemainig + packRemainig;
    }
}
