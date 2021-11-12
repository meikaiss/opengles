package com.demo.opengles.yuv;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;

public class YuvRenderer extends BaseYuvRenderObject {

    private int uMatrixLocation;
    private int uSamplerYLocation;
    private int uSamplerUVLocation;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mRotateMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int yTextureId;
    private int uvTextureId;
    private ByteBuffer yBuffer = null;
    private ByteBuffer uvBuffer = null;

    private byte[] yuvData;
    private int yuvWidth;
    private int yuvHeight;

    /**
     * 将图像顺时针旋转的角度
     * 默认情况下相机在屏幕的成像会顺时针旋转90度（取决于相机硬件），所以这里再叠加顺时针旋转270度，使图像在竖屏手机上正确显示
     * （不考虑手机实体在物理世界的旋转，若需要无论怎样旋转手机都能使图像角度正确，则需要使用重力传感器来实时计算旋转角度）
     */
    public int orientation = 270;

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public YuvRenderer(Context context) {
        super(context);
        initShaderFileName("render/yuv/vertex.frag", "render/yuv/frag.frag");
    }

    public void prepareYuvFrame(byte[] data, int width, int height) {
        yuvData = data;
        yuvWidth = width;
        yuvHeight = height;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        uMatrixLocation = GLES20.glGetUniformLocation(program, "uMatrix");
        uSamplerYLocation = GLES20.glGetUniformLocation(program, "y_texture");
        uSamplerUVLocation = GLES20.glGetUniformLocation(program, "uv_texture");
    }

    private void calculateMatrix() {
        if (yuvWidth <= 0 || yuvHeight <= 0) {
            Matrix.setIdentityM(mMVPMatrix, 0);
            return;
        }

        int previewWidth = yuvWidth;
        int previewHeight = yuvHeight;
        if (orientation == 90 || orientation == 270) {
            previewWidth = yuvHeight;
            previewHeight = yuvWidth;
        }

        //设置正交投影参数
        /**
         * 正交投影用于解决绘制目标宽高与View宽高不一致时引起的变形
         * 固定某一边仍然使用归一化坐标，即[-1,1]
         * 另一边扩大或缩小归一，例如修改坐标范围到delta=[-0.5,0.5]、[-1.5,1.5]
         * 但纹理坐标的范围仍然是[-1,1]，此时映射到delta坐标内即不会变形，但会裁剪或空余
         */
        float sWHImage = previewWidth / (float) previewHeight;
        float sWHView = width / (float) height;
        if (width > height) {
            if (sWHImage > sWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -sWHView * sWHImage, sWHView * sWHImage,
                        -1, 1, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWHView / sWHImage, sWHView / sWHImage,
                        -1, 1, -1, 1);
            }
        } else {
            if (sWHImage > sWHView) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWHView * sWHImage,
                        1 / sWHView * sWHImage, -1, 1);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWHImage / sWHView,
                        sWHImage / sWHView, -1, 1);
            }
        }

        //此行仅用于调试预览尺寸时使用，正常情况不需要此行
//        Matrix.setIdentityM(mProjectMatrix, 0);

        /**
         * 后置摄像头的硬件固定与手机竖屏方向逆时针旋转90度，
         * 通过调整摄像机的上方向为方向，来解决此问题
         * 摄像机有三个参数：摄像机位置坐标、摄像机视线的朝向点、摄像机的上方向点
         */
        float upX = 0f;
        float upY = 1f;

        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.0f,
                0f, 0f, 0f, upX, upY, 0f);

        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        //对总体矩阵旋转指定角度
        Matrix.setIdentityM(mRotateMatrix, 0);
        Matrix.rotateM(mRotateMatrix, 0, orientation, 0, 0, -1);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mRotateMatrix, 0);
    }

    @Override
    protected void bindExtraGLEnv() {
        super.bindExtraGLEnv();

        if (yuvData == null) {
            return;
        }

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

        calculateMatrix();
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
    }

}
