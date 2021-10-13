package com.demo.opengles.gaussian.view.provider;

import android.graphics.Bitmap;

/**
 * 创建原始的、清晰的、等待高斯模糊的图片
 */
public interface IBitmapProvider {

    /**
     * @return 原始的、清晰的、等待高斯模糊的图片
     */
    Bitmap getOriginBitmap();

}
