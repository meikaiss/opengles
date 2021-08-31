package com.demo.opengles.gaussian;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class DefaultRenderObject extends BaseRenderObject {

    public DefaultRenderObject(Context context) {
        super(context);
    }

}
