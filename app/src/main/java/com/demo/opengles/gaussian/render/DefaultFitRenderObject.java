package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存的正中间且不变形，即允许保持宽高比例的缩放，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class DefaultFitRenderObject extends BaseRenderObject {

    //着色器的句柄
    public int uMatrixLocation;

    public float[] mViewMatrix = new float[16];
    public float[] mProjectMatrix = new float[16];
    public float[] mMVPMatrix = new float[16];

    public int inputWidth;
    public int inputHeight;

    public DefaultFitRenderObject(Context context) {
        super(context);
        initShaderFileName("render/base/matrix/vertex.frag", "render/base/matrix/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uMatrixLocation = GLES20.glGetUniformLocation(program, "uMatrix");
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);

        int w = inputWidth;
        int h = inputHeight;
        float scaleWHBmp = w / (float) h;
        float scaleWHView = width / (float) height;
        if (width > height) {
            if (scaleWHBmp > scaleWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -scaleWHView * scaleWHBmp, scaleWHView * scaleWHBmp,
                        -1, 1, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -scaleWHView / scaleWHBmp, scaleWHView / scaleWHBmp,
                        -1, 1, -1, 1);
            }
        } else {
            if (scaleWHBmp > scaleWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / scaleWHView * scaleWHBmp,
                        1 / scaleWHView * scaleWHBmp, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -scaleWHBmp / scaleWHView,
                        scaleWHBmp / scaleWHView, -1, 1);
            }
        }

//        Matrix.setIdentityM(mProjectMatrix, 0);

        /**
         * 后置摄像头的硬件固定与手机竖屏方向逆时针旋转90度，
         * 通过调整摄像机的上方向为x方向，来解决此问题
         * 摄像机有三个参数：摄像机位置坐标、摄像机视线的朝向点、摄像机与视线垂直面的上方向点
         */
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.0f,
                0f, 0f, 0f, 0f, 1f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0,
                mProjectMatrix, 0, mViewMatrix, 0);


//        float[] transLate = new float[16];
//        Matrix.setIdentityM(transLate, 0);
//        Matrix.translateM(transLate, 0, 0f, -0.3f, 0f);
//        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, transLate, 0);
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);
    }
}
