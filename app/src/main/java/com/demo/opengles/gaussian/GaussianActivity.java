package com.demo.opengles.gaussian;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.util.BitmapUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/08/29.
 */
public class GaussianActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private HBlurRenderObject renderObjectH;
    private HBlurRenderObject renderObjectV;
    private DefaultRenderObject defaultRenderObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaussian_2);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        renderObjectH = new HBlurRenderObject(this);
        renderObjectH.setBlurOffset(5, 0);
        renderObjectH.isBindFbo = true;

        renderObjectV = new HBlurRenderObject(this);
        renderObjectV.setBlurOffset(0, 5);
        renderObjectV.isBindFbo = true;

        defaultRenderObject = new DefaultRenderObject(this);
        defaultRenderObject.isBindFbo = false;

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {

            private int textureId;

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                renderObjectH.onCreate();
                renderObjectV.onCreate();
                defaultRenderObject.onCreate();

                textureId = createTexture();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                renderObjectH.onChange(width, height);
                renderObjectV.onChange(width, height);
                defaultRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                renderObjectH.onDraw(textureId);
                renderObjectV.onDraw(renderObjectH.fboTextureId);
                defaultRenderObject.onDraw(renderObjectV.fboTextureId);
            }

            private int createTexture() {
                int[] texture = new int[1];
                Bitmap textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo);
                Bitmap cutBitmap = BitmapUtil.cropBitmapCustom(textureBmp, 500, 500, 400, 400);

                if (textureBmp != null && !textureBmp.isRecycled()) {
                    //在显卡的纹理硬件组上选择当前活跃的纹理单元为：第0号纹理单元，默认为0
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
                    GLES20.glGenTextures(1, texture, 0);
                    textureId = texture[0];
                    //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
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
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBmp, 0);

                    //返回生成的纹理的句柄
                    return texture[0];
                }
                return 0;
            }
        });


        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}
