package com.example.podstreamsdkexample;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.util.Arrays;

import ir.fanap.podstream.offlinestream.PodStream;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        int[] bytes = new int[100];

        for (int i = 0; i < 100; i++)
            bytes[i] = i;
        System.out.println("data: " + Arrays.toString(bytes));
        int start = 0;
        boolean flag = false;
        while (true) {
            int length = 7;

            if (start + length > bytes.length) {
                length = bytes.length - start;
                flag = true;
            }
            int[] temp = new int[length];
            System.arraycopy(bytes, start, temp, 0, length);
            start = start + length;
            System.out.println("data: " + Arrays.toString(temp));
            if (flag)
                break;


        }

        System.out.println("start: " + start);
    }
}