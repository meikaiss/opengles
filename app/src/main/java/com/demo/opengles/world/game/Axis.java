package com.demo.opengles.world.game;

import android.content.Context;

import com.demo.opengles.world.common.Line;
import com.demo.opengles.world.base.WorldObject;

import javax.microedition.khronos.opengles.GL10;

public class Axis extends WorldObject {

    private Line lineX;
    private Line lineY;
    private Line lineZ;

    public Axis(Context context) {
        super(context);
        lineX = new Line(context);
        lineY = new Line(context);
        lineZ = new Line(context);
    }

    @Override
    public void create() {
        lineY.setVertexCoord(0, 0, 0, 1, 0, 0);
        lineX.setColor(1, 0, 0, 1, 1, 0, 0, 1);
        lineX.create();

        lineY.setColor(0, 1, 0, 1, 0, 1, 0, 1);
        lineY.setVertexCoord(0, 0, 0, 0, 1, 0);
        lineY.create();

        lineZ.setColor(0, 0, 1, 1, 0, 0, 1, 1);
        lineZ.setVertexCoord(0, 0, 0, 0, 0, 1);
        lineZ.create();

        lineX.setScale(100, 1, 1);
        lineY.setScale(1, 100, 1);
        lineZ.setScale(1, 1, 100);
    }

    @Override
    public void change(GL10 gl, int width, int height) {
        lineX.change(gl, width, height);
        lineY.change(gl, width, height);
        lineZ.change(gl, width, height);

    }

    @Override
    public void draw(float[] MVPMatrix) {
        lineX.draw(MVPMatrix);
        lineY.draw(MVPMatrix);
        lineZ.draw(MVPMatrix);
    }
}
