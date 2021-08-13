package com.demo.opengles.util;

import android.widget.Toast;

import com.demo.opengles.main.OpenGlApplication;

public class ToastUtil {

    public static void show(String msg) {
        Toast.makeText(OpenGlApplication.instance, msg, Toast.LENGTH_SHORT).show();
    }
}
