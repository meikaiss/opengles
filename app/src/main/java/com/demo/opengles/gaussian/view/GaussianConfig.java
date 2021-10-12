package com.demo.opengles.gaussian.view;

import android.graphics.drawable.Drawable;

public class GaussianConfig {

    /**
     * 模糊的半径，代表上下左右四个方向中，每个方向上采样的像素点个数，单位：像素
     */
    public int blurRadius;

    /**
     * 模糊的水平步长，代表水平方向每N个像素进行一次正太分布采样。值为0时代表水平方向对当前像素进行重复采样，等价于没有模糊
     */
    public float blurOffsetW;

    /**
     * 模糊的垂直步长，代表垂直方向每N个像素进行一次正太分布采样。值为0时代表垂直方向对当前像素进行重复采样，等价于没有模糊
     */
    public float blurOffsetH;

    /**
     * 描述异形模糊的形状Drawable，为空时会直接将矩形原图进行模糊而不裁剪
     */
    public Drawable clipDrawable;

    /**
     * 原始清晰图片提供器。生成图片的方式例如:View截图、系统底层截图、资源文件加载等。
     */
    public IBitmapProvider bitmapProvider;

    public BitmapCreateMode bitmapCreateMode;

    /**
     * 创建原始清晰图片的时机
     */
    public enum BitmapCreateMode {
        /**
         * GaussianGLSurfaceView 初始化时立即创建原始Bitmap，适用于只能在主线程执行的图片提供器
         */
        ViewInit,
        /**
         * 在GlSurfaceView的onDraw里创建原始Bitmap，适用于允许在子线程执行的图片提供器
         */
        GlDraw,

    }

    public static GaussianConfig createDefaultConfig() {
        GaussianConfig gaussianConfig = new GaussianConfig();
        gaussianConfig.blurRadius = 30;
        gaussianConfig.blurOffsetW = 5;
        gaussianConfig.blurOffsetH = 5;

        gaussianConfig.bitmapCreateMode = BitmapCreateMode.GlDraw;
        return gaussianConfig;
    }

}
