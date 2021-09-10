package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.demo.opengles.util.OpenGLESUtil;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class WaterMarkRenderObject extends DefaultRenderObject {
    public String waterMarkText = "水印测试";
    public Bitmap waterMarkBmp;

    public RenderGLInfo renderGLInfo;

    //着色器的句柄
    public int uMatrixLocation2;
    public int aPosLocation2;
    public int aCoordinateLocation2;
    public int uSamplerLocation2;

    public void setWaterMarkText(String waterMarkText) {
        this.waterMarkText = waterMarkText;
    }

    public WaterMarkRenderObject(Context context) {
        super(context);
        renderGLInfo = new RenderGLInfo();
        renderGLInfo.initShaderFileName("render/base/watermark/vertex.frag", "render/base/watermark/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        renderGLInfo.createProgram(context, isBindFbo);

        uMatrixLocation2 = GLES20.glGetAttribLocation(renderGLInfo.program, "uMatrix");
        aPosLocation2 = GLES20.glGetAttribLocation(renderGLInfo.program, "aPos");
        aCoordinateLocation2 = GLES20.glGetAttribLocation(renderGLInfo.program, "aCoordinate");
        uSamplerLocation2 = GLES20.glGetUniformLocation(renderGLInfo.program, "uSampler");


        //启用透明色功能
        GLES20.glEnable(GLES20.GL_BLEND);
        //当纹理叠加时，采用叠加色的alpha值作为生效值
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);
        if (isBindFbo) {
            renderGLInfo.createFBO(width, height);
        }

        waterMarkBmp = OpenGLESUtil.createTextImage(waterMarkText, 28, "#fff000", "#00000000", 0);
        renderGLInfo.textureId = OpenGLESUtil.createWaterTextureId(waterMarkBmp);

        float scale = (float) waterMarkBmp.getWidth() / waterMarkBmp.getHeight();
        float showHeight = 0.1f;
        float showWidth = scale * showHeight;//假定高度为顶点坐标系的0.1尺寸，那么宽度即为scale*0.1
        float startX = -0.9f;
        float startY = -0.9f;
        float viewScale = (float) width / height;
        float[] data = {
                startX, startY,
                startX, startY - showHeight * viewScale,
                startX + showWidth, startY,
                startX + showWidth, startY - showHeight * viewScale
        };

        renderGLInfo.vertexBuffer = OpenGLESUtil.createFloatBuffer(data);
        if (isBindFbo) {
            renderGLInfo.coordinateBuffer = OpenGLESUtil.getSquareCoordinateReverseBuffer();
        } else {
            renderGLInfo.coordinateBuffer = OpenGLESUtil.getSquareCoordinateBuffer();
        }

        renderGLInfo.vboId = OpenGLESUtil.getVbo(renderGLInfo.vertexBuffer, renderGLInfo.coordinateBuffer);

        /////////////////////////////////////////////
        //创建投影矩阵
        /////////////////////////////////////////////
        int w = waterMarkBmp.getWidth();
        int h = waterMarkBmp.getHeight();
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(renderGLInfo.mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH,
                        -1, 1, -1, 1);
            } else {
                Matrix.orthoM(renderGLInfo.mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH,
                        -1, 1, -1, 1);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(renderGLInfo.mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH,
                        1 / sWidthHeight * sWH, -1, 1);
            } else {
                Matrix.orthoM(renderGLInfo.mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight,
                        sWH / sWidthHeight, -1, 1);
            }
        }

        /**
         * 后置摄像头的硬件固定与手机竖屏方向逆时针旋转90度，
         * 通过调整摄像机的上方向为x方向，来解决此问题
         * 摄像机有三个参数：摄像机位置坐标、摄像机视线的朝向点、摄像机与视线垂直面的上方向点
         */
        Matrix.setLookAtM(renderGLInfo.mViewMatrix, 0, 0.0f, 0.0f, 1.0f,
                0f, 0f, 0f, 1f, 0f, 0f);
        //计算变换矩阵
        Matrix.multiplyMM(renderGLInfo.mMVPMatrix, 0,
                renderGLInfo.mProjectMatrix, 0, renderGLInfo.mViewMatrix, 0);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        if (isBindFbo) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fboTextureId, 0);
            GLES20.glViewport(0, 0, width, height);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, renderGLInfo.vboId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderGLInfo.textureId);
        GLES20.glUniform1i(uSamplerLocation2, 0);

        float[] mMatrixTranslate = new float[16];
        Matrix.setIdentityM(mMatrixTranslate, 0);
        Matrix.translateM(mMatrixTranslate, 0, 0.3f, 0, 0);
        Matrix.multiplyMM(renderGLInfo.mMVPMatrix, 0,
                renderGLInfo.mMVPMatrix, 0, mMatrixTranslate, 0);

        GLES20.glUniformMatrix4fv(uMatrixLocation2, 1, false, renderGLInfo.mMVPMatrix, 0);

        GLES20.glEnableVertexAttribArray(aPosLocation2);
        GLES20.glEnableVertexAttribArray(aCoordinateLocation2);

        GLES20.glVertexAttribPointer(aPosLocation2, renderGLInfo.vertexSize, GLES20.GL_FLOAT, false,
                renderGLInfo.vertexStride, 0);
        GLES20.glVertexAttribPointer(aCoordinateLocation2, renderGLInfo.coordinateSize, GLES20.GL_FLOAT, false,
                renderGLInfo.coordinateStride, renderGLInfo.vertexBuffer.limit() * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, renderGLInfo.vertexCount);

        GLES20.glDisableVertexAttribArray(aPosLocation2);
        GLES20.glDisableVertexAttribArray(aCoordinateLocation2);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
