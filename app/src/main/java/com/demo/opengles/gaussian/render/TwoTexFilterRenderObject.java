package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * 将输入的两个纹理渲染到屏幕或离屏缓存上，并以Drawable的形状进行裁剪，Drawable外部的区域为纹理1，内部区域为输入纹理2与Drawable的叠加色
 * Created by meikai on 2021/08/29.
 */
public class TwoTexFilterRenderObject extends BaseRenderObject {

    //第2个纹理的句柄，注：第1个纹理的句柄在基类中
    private int uSampler2Location;
    //Drawable的纹理句柄
    private int uSamplerDrawable;

    //第2个纹理的id，注：第1个纹理的id在基类中
    private int texture2Id;
    //裁剪样板Drawable的纹理id
    private int clipDrawableTextureId;

    private Drawable drawable;
    private int drawableWidth;
    private int drawableHeight;

    public TwoTexFilterRenderObject(Context context, Drawable drawable,
                                    int drawableWidth, int drawableHeight) {
        super(context);
        this.drawable = drawable;
        this.drawableWidth = drawableWidth;
        this.drawableHeight = drawableHeight;
        initShaderFileName("render/two/vertex.frag", "render/two/frag.frag");
    }

    public void setTexture2Id(int texture2Id) {
        this.texture2Id = texture2Id;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uSampler2Location = GLES20.glGetUniformLocation(program, "uSampler2");
        uSamplerDrawable = GLES20.glGetUniformLocation(program, "uSamplerDrawable");
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);

        Bitmap clipBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(clipBmp);
        int left = (width - drawableWidth) / 2;
        int top = (height - drawableHeight) / 2;
        drawable.setBounds(left, top, left + drawableWidth, top + drawableHeight);
        drawable.draw(canvas);

        //默认的0号纹理引脚已经绑定作为输入源，所以这里内部额外的纹理使用2号引脚
        clipDrawableTextureId = createTexture(clipBmp, GLES20.GL_TEXTURE2);
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();


        //启用渲染引擎的1号引脚
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //把第2个纹理id绑定到渲染引擎的1号引脚上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2Id);
        //把1号引脚上绑定的纹理赋值给渲染程序的2号纹理句柄
        GLES20.glUniform1i(uSampler2Location, 1);

        //把2号引脚上绑定的纹理赋值给渲染程序的Drawable形状纹理句柄
        GLES20.glUniform1i(uSamplerDrawable, 2);
    }

    private int createTexture(Bitmap bitmap, int textureIndex) {
        int[] texture = new int[1];

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
        GLES20.glActiveTexture(textureIndex);
        //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
        GLES20.glGenTextures(1, texture, 0);
        //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        //给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        //返回生成的纹理的句柄
        return texture[0];
    }
}
