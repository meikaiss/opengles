package com.demo.opengles.world;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class World {

    private int width;
    private int height;

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    void create() {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    void change(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        setMatrix();
    }

    void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (inTouch) {
            setMatrix();
        }
    }

    void setMatrix() {
        //计算宽高比
        float ratio = (float) width / height;
        /**
         * 设置透视投影
         *
         * 需要注意的是 near 和 far 变量的值必须要大于 0。因为它们都是相对于视点的距离，也就是照相机的距离。
         *
         * 由于透视投影会产生近大远小的效果，当照相机位置不变，改变 near 的值时也会改变物体大小，near 越小，则离视点越近，相当于物体越远，那么显示的物体也就越小了。
         *
         * 当然也可以 near 和 far 的距离不动，改变摄像机的位置来改变观察到的物体大小。
         */
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 50);
        /**
         * 设置相机位置
         *
         * 当用视图矩阵确定了照相机的位置时，要确保物体距离视点的位置在 near 和 far 的区间范围内，否则就会看不到物体。
         */

        float eyeX = (float) (Math.sin((angle - angleDelta) * Math.PI / 180) * 10f);
        float eyeZ = (float) Math.cos((angle - angleDelta) * Math.PI / 180) * 10f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, 10f, eyeZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }


    boolean inTouch;

    float angleDelta = 0;
    float angle = 45;


}

