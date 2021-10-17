package com.demo.opengles.world;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class World {
    private static final String TAG = "World";

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
        resetMatrix();
    }

    void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (resetMatrixFlag) {
            resetMatrix();
            resetMatrixFlag = false;
        }
    }


    public boolean resetMatrixFlag;

    float angleXDelta = 0;
    float angleX = 45; //半径在xz面的投影线段与x轴的夹角
    float angleYDelta = 0;
    float angleY = 45; //半径与xz面的夹角
    public float eyeRadius = 15f; //眼睛与世界坐标原点的距离


    void resetMatrix() {
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
         *
         */
        float realAngleY = (angleY + angleYDelta) % 360; // 值域=[-360, 360]
        realAngleY = (realAngleY + 360) % 360; // 值域=[0, 360]
        float realAngleX = (angleX + angleXDelta * ((realAngleY > 90 && realAngleY < 270) ? -1 : 1)) % 360; // 值域=[-360, 360]
        realAngleX = (realAngleX + 360) % 360; // 值域=[0, 360]

//        Log.e(TAG, "ax = " + realAngleX + ", ay = " + realAngleY);

        float eyeX = (float) (eyeRadius * Math.cos(realAngleY * Math.PI / 180) * Math.cos(realAngleX * Math.PI / 180));
        float eyeY = (float) (eyeRadius * Math.sin(realAngleY * Math.PI / 180));
        float eyeZ = (float) (eyeRadius * Math.cos(realAngleY * Math.PI / 180) * Math.sin(realAngleX * Math.PI / 180));

//        Log.e(TAG, "eyeX = " + eyeX + ", eyeY = " + eyeY + ", eyeZ = " + eyeZ);

        float upY = 1.0f;
        if (realAngleY > 90 && realAngleY < 270) {
            upY = -1f;
        }
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, upY, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }


    float downX;
    float downY;

    public boolean onTouch(MotionEvent event) {
        this.resetMatrixFlag = true;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

            float deltaX = event.getX() - downX;
            float deltaY = event.getY() - downY;

            int factor = 180; //滑动满屏宽度时，表示旋转180度
            angleXDelta = deltaX / getWidth() * factor;
            angleYDelta = deltaY / getWidth() * factor;


        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            angleY = (angleY + angleYDelta) % 360; // 值域=[-360, 360]
            angleY = (angleY + 360) % 360; // 值域=[0, 360]
            angleYDelta = 0;

            angleX = (angleX + angleXDelta * ((angleY > 90 && angleY < 270) ? -1 : 1)) % 360; // 值域=[-360, 360]
            angleX = (angleX + 360) % 360; // 值域=[0, 360]
            angleXDelta = 0;
        }
        return true;
    }

}