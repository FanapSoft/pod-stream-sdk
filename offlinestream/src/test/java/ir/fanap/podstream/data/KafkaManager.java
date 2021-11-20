package ir.fanap.podstream.data;

import ir.fanap.podstream.data.model.Packet;

public class KafkaManager {
    EventListener.KafkaListener listener;
    int[] fileBytes;
    int fileSize;
    int readPosition;

    public KafkaManager setListener(EventListener.KafkaListener listener) {
        this.listener = listener;
        return this;
    }

    public void prepare() {
        fileBytes = new int[1000];
        for (int i = 0; i < fileBytes.length; i++) {
            fileBytes[i] = i;
        }

        listener.fileReady(getFileSize());
    }

    public int getFileSize() {
        fileSize = fileBytes.length;
        return fileSize;
    }

    public void read(int offset, int length) {
        if (offset + length == fileSize) {
            listener.isEnd(true);
        } else if (offset + length > fileSize) {
            {
                listener.isEnd(true);
            }
        }
        int[] bytes = new int[length];
        System.arraycopy(fileBytes, readPosition, bytes, 0, length);
        Packet packet = new Packet(readPosition, (readPosition + length) - 1, bytes);
        readPosition = readPosition + length;
        listener.write(packet);
    }

}
