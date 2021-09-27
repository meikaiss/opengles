package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * 将外界传入的相机纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class CameraRenderObject extends BaseRenderObject {

    private static final String TAG = "CameraRenderObject";

    /**
     * 相机采集到的图像的宽度，单位：像素
     */
    public int inputWidth;
    /**
     * 相机采集到的图像的高度，单位：像素
     */
    public int inputHeight;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int uMatrixLocation;

    public CameraRenderObject(Context context) {
        super(context);
        initShaderFileName("render/base/camera/vertex.frag", "render/base/camera/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uMatrixLocation = GLES20.glGetUniformLocation(program, "uMatrix");
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);

        if (inputWidth == 0 || inputHeight == 0) {
            Log.e(TAG, "相机预览渲染时采用了正交投影矩阵变换，用来防止图像变形。如果不设置预览图像宽度，就无法计算正交投影矩阵，从而导致预览的x或y轴范围是0，最终导致没有显示出任何内容");
        }

        //设置正交投影参数
        /**
         * 正交投影用于解决绘制目标宽高与View宽高不一致时引起的变形
         * 固定某一边仍然使用归一化坐标，即[-1,1]
         * 另一边扩大或缩小归一，例如修改坐标范围到delta=[-0.5,0.5]、[-1.5,1.5]
         * 但纹理坐标的范围仍然是[-1,1]，此时映射到delta坐标内即不会变形，但会裁剪或空余
         */
        int previewWidth;
        int previewHeight;

        /**
         * 为了解决后置相机硬件固定与手机竖屏方向成90度问题
         * 直接将相机矩阵的上方向修改为x正方向
         * 因此宽高需要倒置
         */
        previewWidth = inputHeight;
        previewHeight = inputWidth;

        float sWHImage = previewWidth / (float) previewHeight;
        float sWHView = width / (float) height;
        if (width > height) {
            if (sWHImage > sWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -sWHView * sWHImage, sWHView * sWHImage,
                        -1, 1, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWHView / sWHImage, sWHView / sWHImage,
                        -1, 1, -1, 1);
            }
        } else {
            if (sWHImage > sWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWHView * sWHImage,
                        1 / sWHView * sWHImage, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWHImage / sWHView,
                        sWHImage / sWHView, -1, 1);
            }
        }

        //此行仅用于调试预览尺寸时使用，正常情况不需要此行
//        Matrix.setIdentityM(mProjectMatrix, 0);

        /**
         * 后置摄像头的硬件固定与手机竖屏方向逆时针旋转90度，
         * 通过调整摄像机的上方向为x方向，来解决此问题
         * 摄像机有三个参数：摄像机位置坐标、摄像机视线的朝向点、摄像机与视线垂直面的上方向点
         */
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.0f,
                0f, 0f, 0f, 1f, 0f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
    }

}
