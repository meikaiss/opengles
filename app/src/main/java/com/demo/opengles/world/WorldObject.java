package com.demo.opengles.world;

import android.content.Context;
import android.opengl.Matrix;

public abstract class WorldObject {

    protected Context context;

    public float[] translateMatrix;
    public float[] scaleMatrix;
    public float[] rotateMatrix;

    public WorldObject(Context context) {
        this.context = context;
    }

    public void setTranslate(float x, float y, float z) {
        translateMatrix = new float[16];
        Matrix.setIdentityM(translateMatrix, 0);
        Matrix.translateM(translateMatrix, 0, x, y, z);
    }

    public void addTranslate(float x, float y, float z) {
        if (translateMatrix == null) {
            translateMatrix = new float[16];
            Matrix.setIdentityM(translateMatrix, 0);
        }
        Matrix.translateM(translateMatrix, 0, x, y, z);
    }

    public void setRotate(float a, float x, float y, float z) {
        rotateMatrix = new float[16];
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.rotateM(rotateMatrix, 0, a, x, y, z);
    }

    public void addRotate(float a, float x, float y, float z) {
        if (rotateMatrix == null) {
            rotateMatrix = new float[16];
            Matrix.setIdentityM(rotateMatrix, 0);
        }
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
            Matrix.setIdentityM(scaleMatrix, 0);
        }
        Matrix.scaleM(scaleMatrix, 0, x, y, z);
    }

    public float[] getWorldMatrix() {
        return MatrixHelper.multiplyMM(translateMatrix, scaleMatrix, rotateMatrix);
    }

}
