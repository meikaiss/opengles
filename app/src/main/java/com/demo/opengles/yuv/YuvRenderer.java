package com.demo.opengles.yuv;

import android.content.Context;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;

public class YuvRenderer extends BaseYuvRenderObject {

    private int uSamplerYLocation;
    private int uSamplerUVLocation;

    private int yTextureId;
    private int uvTextureId;

    public YuvRenderer(Context context) {
        super(context);
        initShaderFileName("render/yuv/vertex.frag", "render/yuv/frag.frag");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uSamplerYLocation = GLES20.glGetUniformLocation(program, "y_texture");
        uSamplerUVLocation = GLES20.glGetUniformLocation(program, "uv_texture");
    }

    private byte[] yuvData;
    private int yuvWidth;
    private int yuvHeight;

    public void prepareYuvFrame(byte[] data, int width, int height) {
        yuvData = data;
        yuvWidth = width;
        yuvHeight = height;
    }

    private ByteBuffer yBuffer = null;
    private ByteBuffer uvBuffer = null;

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();

        if (yuvData != null) {
            if (yBuffer == null) {
                yBuffer = ByteBuffer.allocate(yuvWidth * yuvHeight);
            }
            if (uvBuffer == null) {
                uvBuffer = ByteBuffer.allocate(yuvWidth * yuvHeight / 2);
            }

            yBuffer.put(yuvData, 0, yuvWidth * yuvHeight);
            yBuffer.position(0);

            uvBuffer.put(yuvData, yuvWidth * yuvHeight, yuvWidth * yuvHeight / 2);
            uvBuffer.position(0);

            OpenGLESUtil.deleteTextureId(yTextureId);
            //y平面的图像数据的宽高等于真实宽高
            yTextureId = OpenGLESUtil.createTextureId(yBuffer, yuvWidth, yuvHeight);

            OpenGLESUtil.deleteTextureId(uvTextureId);
            //uv平面的图像数据的宽高只有图像真实宽高的一半
            uvTextureId = OpenGLESUtil.createTextureId2(uvBuffer, yuvWidth / 2, yuvHeight / 2);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTextureId);
            GLES20.glUniform1i(uSamplerYLocation, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uvTextureId);
            GLES20.glUniform1i(uSamplerUVLocation, 2);
        }

    }
}
