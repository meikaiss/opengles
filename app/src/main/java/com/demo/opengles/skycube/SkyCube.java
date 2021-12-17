package com.demo.opengles.skycube;

import android.content.Context;

import com.demo.opengles.world.base.WorldObject;

import javax.microedition.khronos.opengles.GL10;

public class SkyCube extends WorldObject {

    private SkyCubePlane plane1;
    private SkyCubePlane plane2;
    private SkyCubePlane plane3;
    private SkyCubePlane plane4;
    private SkyCubePlane plane5;
    private SkyCubePlane plane6;

    private final float[] vertexCoord_1 = {
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f
    };

    public SkyCube(Context context) {
        super(context);

        plane1 = new SkyCubePlane(context, vertexCoord_1, 0, 1);
        plane2 = new SkyCubePlane(context, vertexCoord_1, 1, 1);
        plane3 = new SkyCubePlane(context, vertexCoord_1, 2, 1);
        plane4 = new SkyCubePlane(context, vertexCoord_1, 3, 1);
        plane5 = new SkyCubePlane(context, vertexCoord_1, 1, 0);
        plane6 = new SkyCubePlane(context, vertexCoord_1, 1, 2);
    }

    @Override
    public void create() {
        plane1.create();
        plane2.create();
        plane3.create();
        plane4.create();
        plane5.create();
        plane6.create();
    }

    @Override
    public void change(GL10 gl, int width, int height) {
        plane1.change(gl, width, height);
        plane2.change(gl, width, height);
        plane3.change(gl, width, height);
        plane4.change(gl, width, height);
        plane5.change(gl, width, height);
        plane6.change(gl, width, height);

        float scaleFactor = 100;
        plane1.setScale(scaleFactor, scaleFactor, scaleFactor);
        plane2.setScale(scaleFactor, scaleFactor, scaleFactor);
        plane3.setScale(scaleFactor, scaleFactor, scaleFactor);
        plane4.setScale(scaleFactor, scaleFactor, scaleFactor);
        plane5.setScale(scaleFactor, scaleFactor, scaleFactor);
        plane6.setScale(scaleFactor, scaleFactor, scaleFactor);

        plane2.setRotate(90, 0, 0, 1);
        plane3.setRotate(180, 0, 0, 1);
        plane4.setRotate(270, 0, 0, 1);
        plane5.setRotate(-90, 1, 0, 0);
        plane5.addRotate(270, 0, 1, 0);
        plane6.setRotate(90, 1, 0, 0);
        plane6.addRotate(90, 0, 1, 0);
    }

    @Override
    public void draw(float[] MVPMatrix) {
        plane1.draw(MVPMatrix);
        plane2.draw(MVPMatrix);
        plane3.draw(MVPMatrix);
        plane4.draw(MVPMatrix);
        plane5.draw(MVPMatrix);
        plane6.draw(MVPMatrix);
    }

}
