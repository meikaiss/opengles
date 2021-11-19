package com.demo.opengles.util;

import android.util.Log;

public class LogUtil {

    public static void e(String msg) {
        e("", msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(Exception e) {
        Log.e("", e.getMessage(), e);
    }

    public static void e(String tag, Exception e) {
        Log.e(tag, e.getMessage(), e);
    }

}