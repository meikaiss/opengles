package com.demo.opengles.world.base;

import android.content.Context;
import android.opengl.Matrix;

import com.demo.opengles.world.MatrixHelper;

public abstract class WorldObject extends GLObject {

    public float translateX;
    public float translateY;
    public float translateZ;

    public float rotationX;
    public float rotationY;
    public float rotationZ;

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

    public void setRotate(float angle, float x, float y, float z) {
        rotateMatrix = new float[16];
        Matrix.setIdentityM(rotateMatrix, 0);
        Matrix.rotateM(rotateMatrix, 0, angle, x, y, z);
    }

    public void addRotate(float angle, float x, float y, float z) {
        float[] rotateMatrixTemp = new float[16];
        Matrix.setIdentityM(rotateMatrixTemp, 0);
        Matrix.rotateM(rotateMatrixTemp, 0, angle, x, y, z);

        Matrix.multiplyMM(rotateMatrix, 0, rotateMatrix, 0, rotateMatrixTemp, 0);
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

    public float[] getModelMatrix() {
        if (translateMatrix == null) {
            translateMatrix = new float[16];
            Matrix.setIdentityM(translateMatrix, 0);
        }
        if (scaleMatrix == null) {
            scaleMatrix = new float[16];
            Matrix.setIdentityM(scaleMatrix, 0);
        }
        if (rotateMatrix == null) {
            rotateMatrix = new float[16];
            Matrix.setIdentityM(rotateMatrix, 0);
        }
        return MatrixHelper.multiplyMM(translateMatrix, scaleMatrix, rotateMatrix);
    }

}
