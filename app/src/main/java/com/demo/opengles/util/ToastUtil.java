package com.demo.opengles.util;

import android.os.Looper;
import android.widget.Toast;

import com.demo.opengles.main.OpenGlApplication;

public class ToastUtil {

    public static void show(String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(OpenGlApplication.instance, msg, Toast.LENGTH_SHORT).show();
        } else {
            OpenGlApplication.instance.handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OpenGlApplication.instance, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
