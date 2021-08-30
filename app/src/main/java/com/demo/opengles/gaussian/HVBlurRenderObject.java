package com.demo.opengles.gaussian;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by meikai on 2021/08/29.
 */
public class HVBlurRenderObject extends BaseRenderObject {

    private int uBlurRadiusLocation;
    private int uBlurOffsetLocation;
    private int uSumWeightLocation;

    private int scaleRatio;
    private int blurRadius;
    private float blurOffsetW;
    private float blurOffsetH;
    private float sumWeight;

    public HVBlurRenderObject(Context context) {
        super(context);
        initShaderFileName("render/filter/gaussian_blur/vertex.frag",
                "render/filter/gaussian_blur/frag.frag");

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
    public void onDraw(int textureId) {
        super.onDraw(textureId);

        uBlurRadiusLocation = GLES20.glGetUniformLocation(program, "uBlurRadius");
        uBlurOffsetLocation = GLES20.glGetUniformLocation(program, "uBlurOffset");
        uSumWeightLocation = GLES20.glGetUniformLocation(program, "uSumWeight");

        // 计算总权重
        calculateSumWeight();

        Log.e("mk", blurRadius + " , " + blurOffsetW / width + " , "
                + blurOffsetH / height + " , " + sumWeight);

        GLES20.glUniform1i(uBlurRadiusLocation, blurRadius);
        GLES20.glUniform2f(uBlurOffsetLocation, blurOffsetW / width, blurOffsetH / height);
        GLES20.glUniform1f(uSumWeightLocation, sumWeight);
    }


    public void setScaleRatio(int scaleRatio) {
        this.scaleRatio = scaleRatio;
    }

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    public void setBlurOffset(float width, float height) {
        this.blurOffsetW = width;
        this.blurOffsetH = height;
    }

    public void setSumWeight(float sumWeight) {
        Log.d("HBlur", "setSumWeight: " + sumWeight);
        this.sumWeight = sumWeight;
    }

    /**
     * 计算总权重
     */
    private void calculateSumWeight() {
        if (blurRadius < 1) {
            Log.d("HBlur",
                    "calculateSumWeight: blurRadius:" + blurRadius + " w:" + blurOffsetW + " h:" + blurOffsetH);
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
