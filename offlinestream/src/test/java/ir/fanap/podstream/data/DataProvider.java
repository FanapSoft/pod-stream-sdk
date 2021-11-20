package ir.fanap.podstream.data;


public class DataProvider implements EventListener.BufferListener {
    EventListener.ProviderListener listener;
    BufferManager bufferManager;
    int fileSize;

    public DataProvider setListener( EventListener.ProviderListener listener) {
        this.listener = listener;
        return this;
    }

    public void startStreaming() {
        bufferManager = new BufferManager().setListener(this);
        bufferManager.prepare();
    }

    @Override
    public void fileReady(int fileSize) {
        this.fileSize = fileSize;
        listener.providerIsReady(fileSize);
    }

    public void read(int offset, int length) {
        bufferManager.getData(offset, length);
    }

    @Override
    public void write(int[] data) {
        listener.write(data);
    }

    @Override
    public void isEnd(boolean isEnd) {
        listener.isEnd(isEnd);
    }
}
