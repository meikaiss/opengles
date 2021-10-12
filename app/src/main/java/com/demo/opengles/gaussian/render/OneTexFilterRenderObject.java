package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，并以Drawable的形状进行裁剪，Drawable外部的区域为透明色，内部区域为输入纹理与Drawable的叠加色
 * Created by meikai on 2021/08/29.
 */
public class OneTexFilterRenderObject extends BaseRenderObject {

    private int uSampler2Location;
    private int textureIdDrawable;
    private Drawable drawable;

    public OneTexFilterRenderObject(Context context, Drawable drawable) {
        super(context);
        this.drawable = drawable;
        initShaderFileName("render/one/vertex.frag", "render/one/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uSampler2Location = GLES20.glGetUniformLocation(program, "uSampler2");
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);

        Bitmap clipBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (drawable != null) {
            Canvas canvas = new Canvas(clipBmp);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        //默认的0号纹理引脚已经绑定作为输入源，所以这里内部额外的纹理使用1号引脚
        textureIdDrawable = OpenGLESUtil.createBitmapTextureId(clipBmp, GLES20.GL_TEXTURE1);
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();
        int textureIndex = 1;
        GLES20.glUniform1i(uSampler2Location, textureIndex);
    }

}
