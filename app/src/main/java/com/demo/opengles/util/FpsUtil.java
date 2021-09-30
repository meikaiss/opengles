package com.demo.opengles.util;

import android.util.Log;

import java.util.LinkedList;

public class FpsUtil {

    private static final String TAG = "FpsUtil";

    private String name;

    private int maxFrame = 30;
    private LinkedList<Long> frameTimeStamp = new LinkedList<>();

    public FpsUtil(String name) {
        this.name = name;
    }

    public FpsUtil(String name, int maxFrame) {
        this.name = name;
        this.maxFrame = maxFrame;
    }

    public void trigger() {
        long nowTimeStamp = System.currentTimeMillis();
        if (frameTimeStamp.size() < maxFrame) {
            frameTimeStamp.offer(nowTimeStamp);
        } else {
            long s1 = frameTimeStamp.poll();
            frameTimeStamp.offer(nowTimeStamp);

            float fps;
            if (nowTimeStamp <= s1) {
                fps = -1;
            } else {
                fps = (float) maxFrame / (nowTimeStamp - s1) * 1000;
            }

            Log.e(TAG, "帧率统计, " + name + ", fps = " + fps + ", " + System.currentTimeMillis());
        }
    }
}
