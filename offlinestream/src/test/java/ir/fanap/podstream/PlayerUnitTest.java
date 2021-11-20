package ir.fanap.podstream;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.util.Arrays;
import java.util.Random;

import ir.fanap.podstream.data.DataProvider;
import ir.fanap.podstream.data.EventListener;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PlayerUnitTest implements EventListener.ProviderListener {
    int[] fileBytes;
    DataProvider provider;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void startPlaying() {
        provider = new DataProvider().setListener(this);
        provider.startStreaming();
    }


    @Override
    public void providerIsReady(int filSize) {
        fileBytes = new int[filSize];
//        assertEquals(fileBytes.length, filSize);
        Play();
    }

    int readPosition = 0;
    boolean isEnd = false;

    public void Play() {
        provider.read(readPosition, getRandomLength());
    }

    @Override
    public void write(int[] data) {
        System.out.println("offset: " + readPosition + "  length : " + data.length);
        System.arraycopy(data, 0, fileBytes, readPosition, data.length);
        readPosition = readPosition + data.length;
        continueToPlay();
    }

    @Override
    public void isEnd(boolean isEnd) {
        this.isEnd = isEnd;
        assertEquals(true, isEnd);
        System.out.println(Arrays.stream(fileBytes).toArray());

    }

    public void continueToPlay() {
        if (!isEnd) {
            provider.read(readPosition, getRandomLength());
        }
//        else
//            assertEquals(readPosition, fileBytes.length);
    }

    public int getRandomLength() {
        Random rand = new Random();
        int min = 1;
        int max = 20;
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}