package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;

import java.nio.FloatBuffer;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class WaterMarkRenderObject extends DefaultRenderObject {

    public String waterMarkText = "测试";

    public int waterTextureId;

    public FloatBuffer vertexBufferWaterMark;
    public FloatBuffer coordinateBufferWaterMark;
    public int vboIdWaterMark;

    public void setWaterMarkText(String waterMarkText) {
        this.waterMarkText = waterMarkText;
    }

    public WaterMarkRenderObject(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //启用透明色功能
        GLES20.glEnable(GLES20.GL_BLEND);
        //当纹理叠加时，采用叠加色的alpha值作为生效值
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        Bitmap bitmap = OpenGLESUtil.createTextImage("水印", 28, "#fff000", "#00000000", 0);
        waterTextureId = OpenGLESUtil.createWaterTextureId(bitmap);

        float scale = (float) bitmap.getWidth() / bitmap.getHeight();
        float showHeight = 0.1f;
        float showWidth = scale * showHeight;//假定高度为顶点坐标系的0.1尺寸，那么宽度即为scale*0.1
        float startX = -0.9f;
        float startY = 0.9f;
        float[] data = {
                startX, startY,
                startX, startY - showHeight,
                startX + showWidth, startY,
                startX + showWidth, startY - showHeight
        };

        vertexBufferWaterMark = OpenGLESUtil.createFloatBuffer(data);
        if (isBindFbo) {
            coordinateBufferWaterMark = OpenGLESUtil.getSquareCoordinateReverseBuffer();
        } else {
            coordinateBufferWaterMark = OpenGLESUtil.getSquareCoordinateBuffer();
        }
        vboIdWaterMark = OpenGLESUtil.getVbo(vertexBufferWaterMark, coordinateBufferWaterMark);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);

        if (isBindFbo) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fboTextureId, 0);
            GLES20.glViewport(0, 0, width, height);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIdWaterMark);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, waterTextureId);
        GLES20.glUniform1i(uSamplerLocation, 0);

        GLES20.glEnableVertexAttribArray(aPosLocation);
        GLES20.glEnableVertexAttribArray(aCoordinateLocation);

        GLES20.glVertexAttribPointer(aPosLocation, vertexSize, GLES20.GL_FLOAT, false,
                vertexStride, 0);
        GLES20.glVertexAttribPointer(aCoordinateLocation, coordinateSize, GLES20.GL_FLOAT, false,
                coordinateStride, vertexBufferWaterMark.limit() * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(aPosLocation);
        GLES20.glDisableVertexAttribArray(aCoordinateLocation);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
