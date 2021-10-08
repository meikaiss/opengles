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
    /**
     * 硬件层特征，为了让图像在手机屏幕直立显示(即图像上方向与屏幕上方向相同)，需要将图像顺时针旋转此角度。不同手机厂商的此角度值会有不同
     * 据资料记载大多数相机默认需要将图像顺时针旋转90度，这里直接在opengl中处理旋转，而没有通过camera.setDisplayOrientation()或parameters.setRotation()，让外界的调用变得足够简单，让本类强大到包容一切外界变数
     */
    public int orientation = 90;
    /**
     * 是否启用相机上方向与屏幕竖屏上方向一致性调整
     * 默认启用，因为大多数相机都需要把底层传上来的图像顺时针旋转90度，才能在屏幕竖屏上正常显示，这里暂不考虑横竖屏切换，只处理竖屏的显示
     */
    public boolean orientationEnable = false;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int uMatrixLocation;

    public CameraRenderObject(Context context) {
        super(context);
        initShaderFileName("render/camera/vertex.frag", "render/camera/frag.frag");
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

        int previewWidth = inputWidth;
        int previewHeight = inputHeight;

        /**
         * 若相机成像需要顺时针旋转的角度为90或270，则此角度下图像的宽高需要置换
         */
        if (orientationEnable && (orientation == 90 || orientation == 270)) {
            previewWidth = inputHeight;
            previewHeight = inputWidth;
        }

        //设置正交投影参数
        /**
         * 正交投影用于解决绘制目标宽高与View宽高不一致时引起的变形
         * 固定某一边仍然使用归一化坐标，即[-1,1]
         * 另一边扩大或缩小归一，例如修改坐标范围到delta=[-0.5,0.5]、[-1.5,1.5]
         * 但纹理坐标的范围仍然是[-1,1]，此时映射到delta坐标内即不会变形，但会裁剪或空余
         */
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
         * 通过调整摄像机的上方向为方向，来解决此问题
         * 摄像机有三个参数：摄像机位置坐标、摄像机视线的朝向点、摄像机的上方向点
         */
        float upX = 0f;
        float upY = 1f;
        if (orientationEnable) {
            if (orientation == 90) {
                upX = -1f * getFboFactor();
                upY = 0f;
            } else if (orientation == 270) {
                upX = 1f * getFboFactor();
                upY = 0f;
            } else if (orientation == 0) {
                upX = 0f;
                upY = 1f * getFboFactor();
            } else if (orientation == 180) {
                upX = 0f;
                upY = -1f * getFboFactor();
            }
        }

        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.0f,
                0f, 0f, 0f, upX, upY, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    /**
     * 当开启FBO时，需要进行xy轴坐标倒置，待验证此结论
     */
    private int getFboFactor() {
        return isBindFbo ? -1 : 1;
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
    }

}
