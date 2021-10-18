package com.example.podstreamsdkexample;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

import ir.fanap.podstream.util.PodThreadManager;

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


    @Test
    public void splitParts() {
        int[] kafkalist = getArray(1000);
        int bufferlengh = 50;
        int[] start = new int[bufferlengh];
        int[] end = new int[bufferlengh];
        int[] midle = new int[kafkalist.length - (bufferlengh * 2)];

        System.arraycopy(kafkalist, 0, start, 0, bufferlengh);
        System.arraycopy(kafkalist, kafkalist.length - bufferlengh, end, 0, bufferlengh);
        System.arraycopy(kafkalist, start.length, midle, 0, midle.length);
        findofssets(kafkalist, end, 960, 20);
//        System.arraycopy(end, starto, temp, 0, length);
//        //25-26-27
//        System.out.println(Arrays.toString(start));
//        System.out.println(Arrays.toString(end));
    }

    //startleng=1000
    //templegth= 50
    //offset = 960
    //count = 20
    public void getcount(String log) {
        int th = java.lang.Thread.activeCount();
        Log.e("thidfd", "getcount: " + log + th);
    }

    @Test
    public void test() {
        new PodThreadManager()
                .addNewTask(() -> {


                    for (int i = 0; i < 20; i++) {
                        Log.e("loger", "aaa ");

                    }


                })
                .addNewTask(() -> {


                    for (int i = 0; i < 10; i++) {
                        Log.e("loger", "bbb ");

                    }

                })
                .runTasksASync();
    }

    void findofssets(int[] length1, int[] templength1, int offset, int count) {
        int[] temp = new int[count];
        int starto;
        int endo;
        starto = length1.length - (offset + count);
        endo = templength1.length - starto;
        starto = endo - count;
        System.arraycopy(templength1, starto, temp, 0, count);
        System.out.println(starto + " <====start   end====> " + endo);
    }

    int[] getArray(int length) {
        int[] list = new int[length];
        for (int i = 0; i < length; ++i) {
            list[i] = i;
        }
        return list;
    }


}