package com.demo.opengles.world.base;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public abstract class GLObject {

    protected Context context;

    public GLObject(Context context) {
        this.context = context;
    }

    public abstract void create();

    public abstract void change(GL10 gl, int width, int height);

    public abstract void draw(float[] MVPMatrix);

}
