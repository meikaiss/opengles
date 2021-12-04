package com.demo.opengles.world.base;

import android.content.Context;
import android.opengl.Matrix;

import com.demo.opengles.world.MatrixHelper;

public abstract class WorldObject extends GLObject {

    public float translateX;
    public float translateY;
    public float translateZ;

    private float[] translateMatrix;
    private float[] scaleMatrix;
    private float[] rotateMatrix;

    public WorldObject(Context context) {
        super(context);
        this.context = context;
    }

    public void setTranslate(float x, float y, float z) {
        translateX = x;
        translateY = y;
        translateZ = z;
        translateMatrix = new float[16];
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, translateX, translateY, translateZ);
    }

    public void addTranslate(float x, float y, float z) {
        translateX += x;
        translateY += y;
        translateZ += z;
        if (translateMatrix == null) {
            translateMatrix = new float[16];
        }
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, translateX, translateY, translateZ);
    }

    public void setRotate(float a, float x, float y, float z) {
        rotateMatrix = new float[16];
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.rotateM(rotateMatrix, 0, a, x, y, z);
    }

    public void addRotate(float a, float x, float y, float z) {
        if (rotateMatrix == null) {
            rotateMatrix = new float[16];
        }
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.rotateM(rotateMatrix, 0, a, x, y, z);
    }

    public void setScale(float x, float y, float z) {
        scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    public void addScale(float x, float y, float z) {
        if (scaleMatrix == null) {
            scaleMatrix = new float[16];
        }
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    public float[] getWorldMatrix() {
        return MatrixHelper.multiplyMM(translateMatrix, scaleMatrix, rotateMatrix);
    }

}
