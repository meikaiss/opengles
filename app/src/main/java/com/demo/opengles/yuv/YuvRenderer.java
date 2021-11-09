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
            /**
             * 对Buffer中的数据进行采样，依据采样的结果创建一个相同宽高的纹理
             * Y平面的图像数据的宽高等于真实宽高
             * 将Y平面的数据填充到相同宽高的纹理中，纹理中每个像素的RGB都等于Y的值，每个像素的A值都为1
             */
            yTextureId = OpenGLESUtil.createTexture(yBuffer, yuvWidth, yuvHeight);

            OpenGLESUtil.deleteTextureId(uvTextureId);
            /**
             * 官方的解释见下面这段英文
             * 解释：从 uvBuffer 中采样生成一个纹理。
             * UV数据的采样都缩小了一半，因此只需要宽高为一半的纹理，把U和V放进两个区域存储即可。
             * 通过设置 GL_LUMINANCE_ALPHA 标志，opengl会将Buffer中第一个字节的数据存储到像素的RGB通道中，
             * R和G和B三个值都是第一个字节，即V值；第二个字节的数据存储到像素的A通道中。
             */
            /**
             * UV texture is (width/2*height/2) in size (downsampled by 2 in
             * both dimensions, each pixel corresponds to 4 pixels of the Y channel)
             * and each pixel is two bytes. By setting GL_LUMINANCE_ALPHA, OpenGL
             * puts first byte (V) into R,G and B components and of the texture
             * and the second byte (U) into the A component of the texture. That's
             * why we find U and V at A and R respectively in the fragment shader code.
             * Note that we could have also found V at G or B as well.
             */
            uvTextureId = OpenGLESUtil.createTextureWithAlpha(uvBuffer, yuvWidth / 2, yuvHeight / 2);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTextureId);
            GLES20.glUniform1i(uSamplerYLocation, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uvTextureId);
            GLES20.glUniform1i(uSamplerUVLocation, 2);
        }

    }
}
