package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

/**
 * 将外界传入的纹理进行横向、纵向高斯模糊后，再渲染到屏幕或离屏缓存上
 * Created by meikai on 2021/08/29.
 */
public class HVBlurRenderObject extends BaseRenderObject {

    private static final String TAG = "HVBlur";

    private int uBlurRadiusLocation;
    private int uBlurOffsetLocation;
    private int uSumWeightLocation;

    private int scaleRatio;
    //向四周进行采样的圈数，不会受步长影响圈数的大小
    private int blurRadius;
    //水平方向模糊步长（步长=0时表示对当前像素进行{圈数}次采样，步长=1时表示对左右上下{圈数}个像素进行正太分布采样，步长=2类推...）
    private float blurOffsetW;
    //垂直方向模糊步长
    private float blurOffsetH;
    private float sumWeight;

    public float getBlurOffsetW() {
        return blurOffsetW;
    }

    public float getBlurOffsetH() {
        return blurOffsetH;
    }

    public HVBlurRenderObject(Context context) {
        super(context);
        initShaderFileName("render/gaussian/vertex.frag",
                "render/gaussian/frag.frag");

        // 设置缩放因子
        setScaleRatio(1);
        // 设置模糊半径
        setBlurRadius(30);
        // 设置模糊步长
        setBlurOffset(1, 0);
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width / scaleRatio, height / scaleRatio);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uBlurRadiusLocation = GLES20.glGetUniformLocation(program, "uBlurRadius");
        uBlurOffsetLocation = GLES20.glGetUniformLocation(program, "uBlurOffset");
        uSumWeightLocation = GLES20.glGetUniformLocation(program, "uSumWeight");
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();
        // 计算总权重
        calculateSumWeight();

        Log.e(TAG, blurRadius + " , " + blurOffsetW / width + " , "
                + blurOffsetH / height + " , " + sumWeight);

        GLES20.glUniform1i(uBlurRadiusLocation, blurRadius);
        GLES20.glUniform2f(uBlurOffsetLocation, blurOffsetW / width, blurOffsetH / height);
        GLES20.glUniform1f(uSumWeightLocation, sumWeight);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);
    }

    public void setScaleRatio(int scaleRatio) {
        this.scaleRatio = scaleRatio;
    }

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    public void setBlurOffset(float blurOffsetW, float blurOffsetH) {
        this.blurOffsetW = blurOffsetW;
        this.blurOffsetH = blurOffsetH;
    }

    public void setSumWeight(float sumWeight) {
        Log.d(TAG, "setSumWeight: " + sumWeight);
        this.sumWeight = sumWeight;
    }

    /**
     * 计算总权重
     */
    private void calculateSumWeight() {
        if (blurRadius < 1) {
            Log.d(TAG, "calculateSumWeight: blurRadius:" + blurRadius
                    + " w:" + blurOffsetW + " h:" + blurOffsetH);
            setSumWeight(0);
            return;
        }

        float sumWeight = 0;
        float sigma = blurRadius / 3f;
        for (int i = 0; i < blurRadius; i++) {
            float weight =
                    (float) ((1 / Math.sqrt(2 * Math.PI * sigma * sigma)) * Math.exp(-(i * i) / (2 * sigma * sigma)));
            sumWeight += weight;
            if (i != 0) {
                sumWeight += weight;
            }
        }

        setSumWeight(sumWeight);
    }

    @Override
    public void onRelease() {
        super.onRelease();
    }
}
