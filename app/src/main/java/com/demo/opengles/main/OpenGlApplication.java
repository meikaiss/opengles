package com.demo.opengles.main;

import android.app.Application;
import android.content.Context;

public class OpenGlApplication extends Application {

    public static OpenGlApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }
}
