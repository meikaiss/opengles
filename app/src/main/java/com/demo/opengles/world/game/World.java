package com.demo.opengles.world.game;

import android.opengl.GLES20;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class World {
    private static final String TAG = "World";
    private static final float openglIdentitySize = 1.0f; //opengl归一化坐标系的标准宽高，通常定义为1.0

    private int viewWidth;
    private int viewHeight;
    private float worldSizeScale = 1; //世界坐标系的缩放比例，即世界范围的宽高深是归一化坐标的10倍

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public boolean resetMatrixFlag; //重新计算透视矩阵的标记

    public float eyeX = 0f;
    public float eyeY = -20f;
    public float eyeZ = 2f;

    public float scaleFactor = 1.0f;

    public int getWidth() {
        return viewWidth;
    }

    public int getHeight() {
        return viewHeight;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    public void create() {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    public void change(GL10 gl, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
        resetWorldMatrix();
    }

    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (resetMatrixFlag) {
            resetWorldMatrix();
            resetMatrixFlag = false;
        }
    }

    private void resetWorldMatrix() {
        //计算宽高比
        float ratio = (float) viewWidth / viewHeight * openglIdentitySize;
        /**
         * 设置透视投影，用于创建一个锥形视景体
         * 需要注意的是 near 和 far 变量的值必须要大于 0。因为它们都是相对于视点的距离，也就是照相机的距离。
         * 注意：针对透视矩阵，near越小，视觉效果越小；near越大，视觉效果越大。近near近小远大
         *
         * 定义：设置透视投影矩阵，用于定义物理世界中的一个锥形视景体，描述将物理世界中哪一部分范围的景色投影到二维屏幕中。
         * 理解：透视投影中，视点理解为眼睛，近平面理解为屏幕，远平面理解为观察的最远平面，近平面与远平面之间的物体可被投影到近平面（即屏幕）上。视点与近平面的射线在近平面与远平面的部分是四棱锥，这部分四棱锥就是可以显示出的物理世界的物体。视点与近平面越近，则看到的范围越广，则同一个物体在视角范围内的占比就越小，从而投影到近平面上就越小。
         * 奥义：与通常所说的近大远小的结论相反，根本原因在于参照系不同。透视投影矩阵中所说的“近”是指视点与近平面的距离，近平面指的是计算机屏幕。
         * 避坑：在理解透视投影时，暂时不考虑相机矩阵，完全把相机当作固定位置。相机矩阵是在透视投影矩阵的基础上再次包装而得到的算法。
         * 代码：创建方法有两种：frustumM 和 perspectiveM
         */
        Matrix.frustumM(mProjectMatrix, 0,
                -ratio * worldSizeScale, ratio * worldSizeScale,
                -1 * worldSizeScale, 1 * worldSizeScale,
                1f * worldSizeScale, 500 * worldSizeScale);
        /**
         * 设置相机位置
         * 当用视图矩阵确定了照相机的位置时，要确保物体距离视点的位置在 near 和 far 的区间范围内，否则就会看不到物体。
         * 注意：针对相机矩阵，视觉效果为近大远小
         */
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, eyeX, eyeY + 20, eyeZ, 0f, 0f, 1f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        //放大缩小世界
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, scaleMatrix, 0);
    }

    public void move(int deltaX, int deltaY) {
        float radius = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        float speed = 0.05f; //移动控制器满半径时的世界坐标系移动速度为0.05

        eyeX += (speed * deltaX / radius);
        eyeY += (speed * deltaY / radius);

        resetMatrixFlag = true;
    }

    public void onScale(float scaleFactorParam) {
        resetMatrixFlag = true;

        scaleFactor *= scaleFactorParam;
        scaleFactor = Math.max(scaleFactor, 0);
    }

}