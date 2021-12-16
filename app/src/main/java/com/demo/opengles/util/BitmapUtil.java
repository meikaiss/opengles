package com.demo.opengles.util;

import android.graphics.Bitmap;

/**
 * Created by meikai on 2021/08/29.
 */
public class BitmapUtil {

    public static Bitmap cropBitmapCustom(Bitmap srcBitmap, int firstPixelX, int firstPixelY, int needWidth, int needHeight) {

        if (firstPixelX + needWidth > srcBitmap.getWidth()) {
            needWidth = srcBitmap.getWidth() - firstPixelX;
        }

        if (firstPixelY + needHeight > srcBitmap.getHeight()) {
            needHeight = srcBitmap.getHeight() - firstPixelY;
        }

        /**裁剪关键步骤*/
        Bitmap cropBitmap = Bitmap.createBitmap(srcBitmap, firstPixelX, firstPixelY, needWidth, needHeight);

        return cropBitmap;
    }

}
