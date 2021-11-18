package ir.fanap.podstream.util;

public class ThreadEvent {

    private final Object lock = new Object();
    private boolean isAwait = false;

    public boolean isAwait() {
        return isAwait;
    }

    public void signal() {
        synchronized (lock) {
            lock.notify();
            isAwait = false;
        }
    }

    public void await() throws InterruptedException {

        synchronized (lock) {
            isAwait = true;
            lock.wait();
        }
    }
}