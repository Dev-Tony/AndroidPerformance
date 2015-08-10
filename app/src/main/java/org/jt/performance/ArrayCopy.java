package org.jt.performance;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class ArrayCopy extends Activity{

    public static final String TAG = "ArrayCopy";
    public static final int COUNT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_array_copy);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int allLength = 192 * 1024;
                int halfLength = allLength / 2;
                byte[] src = new byte[allLength];
                byte[] dest1 = new byte[halfLength];
                byte[] dest2 = new byte[halfLength];
                Arrays.fill(src, (byte) 10);

                Log.i(TAG, " start arraycopy test");
                long before = System.currentTimeMillis();
                for (int j = 0; j < COUNT; j++) {
                    for (int i = 0; i < halfLength; i += 2) {
                        System.arraycopy(src, i << 1, dest1, i, 2);
                        System.arraycopy(src, (i << 1) + 2, dest2, i, 2);
                    }
                }
                long time = System.currentTimeMillis() - before;
                Log.i(TAG, "System.arraycopy occupy " + time);

                before = System.currentTimeMillis();
                for (int j = 0; j < COUNT; j++) {
                    for (int i = 0; i < halfLength; i += 2) {
                        int index = i << 1;
                        dest1[i] = src[index];
                        dest1[i + 1] = src[index + 1];
                        dest2[i] = src[index + 2];
                        dest2[i + 1] = src[index + 3];
                    }
                }
                time = System.currentTimeMillis() - before;
                Log.i(TAG, "java copy occupy " + time);

                before = System.currentTimeMillis();
                for (int j = 0; j < COUNT; j++) {
                    deliverArray(src, dest1, dest2, allLength);
                }
                time = System.currentTimeMillis() - before;
                Log.i(TAG, "native copy occupy " + time);
            }

        });
    }

    private native boolean deliverArray(byte[] src, byte[] left, byte[] right, int length);
}
