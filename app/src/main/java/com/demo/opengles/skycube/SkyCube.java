package com.demo.opengles.skycube;

import android.content.Context;

import com.demo.opengles.world.base.WorldObject;

import javax.microedition.khronos.opengles.GL10;

public class SkyCube extends WorldObject {

    private SkyCubePlane plane1;

    public SkyCube(Context context) {
        super(context);

        plane1 = new SkyCubePlane(context);
    }

    @Override
    public void create() {
        plane1.create();
    }

    @Override
    public void change(GL10 gl, int width, int height) {
        plane1.change(gl, width, height);


        plane1.setScale(10, 10, 10);
    }

    @Override
    public void draw(float[] MVPMatrix) {
        plane1.draw(MVPMatrix);
    }

}
