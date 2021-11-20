package ir.fanap.podstream.data;

import ir.fanap.podstream.data.model.Packet;

public abstract  class EventListener {
    public interface ProviderListener {
        void providerIsReady(int filSize);
        void write(int[] data);
        void isEnd(boolean isEnd);
    }
    interface KafkaListener {
         void fileReady(int filSize);

        void write(Packet packet);

        void isEnd(boolean isEnd);
    }
    interface BufferListener {
        void fileReady(int filSize);

        void write(int[] data);

        void isEnd(boolean isEnd);
    }
}
