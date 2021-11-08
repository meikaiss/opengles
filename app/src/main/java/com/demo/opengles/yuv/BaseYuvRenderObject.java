package com.demo.opengles.yuv;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.demo.opengles.gaussian.render.IRenderAble;
import com.demo.opengles.util.OpenGLESUtil;

import java.nio.FloatBuffer;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * <p>
 * 将任何需要被子类修改的数据提取抽象方法
 * 作为抽象类规范全部的opengl绘制流程，不允许直接产生对象
 * Created by meikai on 2021/08/29.
 */
public abstract class BaseYuvRenderObject implements IRenderAble {

    private static final String TAG = "BaseYuvRenderObject";

    public Context context;
    public String vertexFilename;
    public String fragFilename;

    public String vertexShaderCode;
    public String fragShaderCode;

    public float[] vertexCoordinate;
    public FloatBuffer vertexBuffer;
    public FloatBuffer coordinateBuffer;
    public int vertexSize = 2;
    public int coordinateSize = 2;
    public int vertexStride = vertexSize * 4;
    public int coordinateStride = coordinateSize * 4;
    public int vertexCount = 4;
    public int coordinateCount = 4;

    public int vertexShader;
    public int fragShader;
    public int program;
    public int textureId;
    public int fboTextureId;
    public int fboId;
    public int vboId;
    //着色器的句柄
    public int aPosLocation;
    public int aCoordinateLocation;
    public int uSamplerLocation;

    public int width;
    public int height;
    public int viewportX;
    public int viewportY;

    public boolean isBindFbo;
    //标记输入的纹理是否为外部纹理，例如相机输入; 启用此标记时，需要设置对应的fragShader language的标记位为samplerExternalOES
    public boolean isOES;
    //标记每次Draw之前是否需要清除画面背景色
    public boolean clearFlag = true;

    public boolean isCreate = false;
    public boolean isChange = false;

    public Object tag;

    public BaseYuvRenderObject(Context context) {
        this.context = context;
    }

    public void initShaderFileName(String vertexFilename, String fragFilename) {
        this.vertexFilename = vertexFilename;
        this.fragFilename = fragFilename;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + this.getClass().getSimpleName());

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(0, 0, 0, 0);


        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 1);

        vertexShaderCode = OpenGLESUtil.getShaderCode(context, vertexFilename);
        fragShaderCode = OpenGLESUtil.getShaderCode(context, fragFilename);

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 2);
        if (vertexCoordinate == null) {
            vertexBuffer = OpenGLESUtil.getSquareVertexBuffer();
        } else {
            vertexBuffer = OpenGLESUtil.createFloatBuffer(vertexCoordinate);
        }

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 3);
        if (isBindFbo) {
            coordinateBuffer = OpenGLESUtil.getSquareCoordinateReverseBuffer();
        } else {
            coordinateBuffer = OpenGLESUtil.getSquareCoordinateBuffer();
        }

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 4);
        vboId = OpenGLESUtil.getVbo(vertexBuffer, coordinateBuffer);

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 5 + "\n" + vertexShaderCode + fragShaderCode);
        vertexShader = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragShader = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderCode);

        program = OpenGLESUtil.linkProgram(vertexShader, fragShader);

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 6);
        aPosLocation = GLES20.glGetAttribLocation(program, "aPos");
        aCoordinateLocation = GLES20.glGetAttribLocation(program, "aCoordinate");
        if (!isOES) {
//            uSamplerLocation = GLES20.glGetUniformLocation(program, "uSampler");
        }

        Log.e(TAG, "onCreate, thread.name=" + Thread.currentThread().getName()
                + " , " + 7);
        isCreate = true;
    }

    @Override
    public void onChange(int width, int height) {
        this.width = width;
        this.height = height;

        if (isBindFbo) {
            int[] fboData = OpenGLESUtil.getFbo(width, height);
            fboId = fboData[0];
            fboTextureId = fboData[1];
        }

        isChange = true;
    }

    /**
     * 给子类扩展的opengl环境变量赋值
     */
    protected void bindExtraGLEnv() {

    }

    @Override
    public void onDraw(int textureId) {
        if (!isCreate || !isChange) {
            throw new IllegalStateException("you must call onCreate and onChange before onDraw");
        }
//        this.textureId = textureId;

        GLES20.glUseProgram(program);

        if (isBindFbo) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
        }

        GLES20.glViewport(viewportX, viewportY, width, height);

        if (clearFlag) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        if (isOES) {
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//        } else {
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//            GLES20.glUniform1i(uSamplerLocation, 0);
//        }

        bindExtraGLEnv();

        GLES20.glEnableVertexAttribArray(aPosLocation);
        GLES20.glEnableVertexAttribArray(aCoordinateLocation);

        GLES20.glVertexAttribPointer(aPosLocation, vertexSize, GLES20.GL_FLOAT, false,
                vertexStride, 0);
        GLES20.glVertexAttribPointer(aCoordinateLocation, coordinateSize, GLES20.GL_FLOAT, false,
                coordinateStride, vertexBuffer.limit() * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(aPosLocation);
        GLES20.glDisableVertexAttribArray(aCoordinateLocation);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void onRelease() {
        GLES20.glDeleteProgram(program);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragShader);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{vboId}, 0);

        GLES20.glDeleteTextures(1, new int[]{fboTextureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{fboId}, 0);
    }
}
