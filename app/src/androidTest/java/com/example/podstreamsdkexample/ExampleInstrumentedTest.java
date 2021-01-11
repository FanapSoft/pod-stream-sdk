package com.example.podstreamsdkexample;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.podstreamsdkexample", appContext.getPackageName());
    }

    @Test
    public void copyArray() {

        int[] kafkalist = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 100};
        int[] temp = new int[kafkalist.length];
        boolean loadEnded = false;

        int offset = 0;
        int readLength = 5;

        while (!loadEnded) {
            try {

                System.arraycopy(kafkalist, offset, temp, offset, readLength);
                System.out.println("offset:before " + (offset + readLength));
                int t = offset + readLength;
                if (readLength != 5) {
                    loadEnded = true;
                } else {
                    if ((kafkalist.length - t) < 5) {
                        offset = offset + readLength;
                        readLength = kafkalist.length - t;
                        System.out.println("is: " + readLength);
                    } else
                        offset = offset + readLength;
                }

            } catch (Exception e) {
                loadEnded = true;
                System.out.println("offset: " + offset + " " + kafkalist.length);

            }
        }
        System.out.println(Arrays.toString(temp));


    }

    @Test
    public void testCopyARRAY() {
        int[] kafkalist = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 100};
        int[] temp = new int[kafkalist.length];
        int length = 17;
        int offset = 6;
        int i;
        int start = 0;
        for (i = offset; start < length; i = i + 3) {
            int newlength = 3;
            if (start + newlength > length)
                newlength = length - start;
            System.arraycopy(kafkalist, i, temp, start, newlength);
            start = start + newlength;
            ;
        }
        System.out.println(Arrays.toString(temp));

    }

}