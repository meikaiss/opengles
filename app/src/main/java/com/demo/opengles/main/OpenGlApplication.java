package com.demo.opengles.main;

import android.app.Application;
import android.content.Context;
import android.os.Handler;


public class OpenGlApplication extends Application {

    public static OpenGlApplication instance;
    public Handler handler = new Handler();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }
}
