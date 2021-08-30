package com.demo.opengles.gaussian;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.demo.opengles.R;

/**
 * Created by meikai on 2021/08/29.
 */
public class DefaultRenderObject extends BaseRenderObject {

    private int uSamplerLocation2;
    public int textureId2;

    public DefaultRenderObject(Context context) {
        super(context);
        initShaderFileName("render/base/base/vertex.frag", "render/base/base/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        uSamplerLocation2 = GLES20.glGetUniformLocation(program, "uSamplerABC");

    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);

        Bitmap clipBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(clipBmp);
        VectorDrawable vectorDrawable =
                (VectorDrawable) context.getResources().getDrawable(R.drawable.ic_svg_test_2);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);


        Bitmap bmpArthur = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.texture_iamge_arthur);
//        textureId2 = createTexture(bmpArthur);
    }

    @Override
    protected void f() {
        super.f();
//        GLES20.glUniform1i(uSamplerLocation2, 1);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);

    }

    private int createTexture(Bitmap bitmap) {
        int[] texture = new int[1];

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
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
