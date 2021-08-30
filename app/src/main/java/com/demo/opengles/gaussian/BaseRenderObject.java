package com.demo.opengles.gaussian;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/08/29.
 */
public abstract class BaseRenderObject implements RenderAble {

    private static final String TAG = "BaseRenderObject";

    public Context context;
    public String vertexFilename;
    public String fragFilename;

    public String vertexShaderCode;
    public String fragShaderCode;

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

    public int width;
    public int height;

    public boolean isBindFbo;

    //着色器的句柄
    public int aPosLocation;
    public int aCoordinateLocation;
    public int uSamplerLocation;

    public boolean isCreate = false;
    public boolean isChange = false;


    public BaseRenderObject(Context context) {
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

        GLES20.glClearColor(0, 0, 0, 1.0f);

        vertexShaderCode = OpenGLESUtils.getShaderCode(context, vertexFilename);
        fragShaderCode = OpenGLESUtils.getShaderCode(context, fragFilename);

        vertexBuffer = OpenGLESUtils.getSquareVertexBuffer();
        if (isBindFbo) {
            coordinateBuffer = OpenGLESUtils.getSquareCoordinateReverseBuffer();
        } else {
            coordinateBuffer = OpenGLESUtils.getSquareCoordinateBuffer();
        }
        vboId = OpenGLESUtils.getVbo(vertexBuffer, coordinateBuffer);

        vertexShader = OpenGLESUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragShader = OpenGLESUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderCode);

        program = OpenGLESUtils.linkProgram(vertexShader, fragShader);

        aPosLocation = GLES20.glGetAttribLocation(program, "aPos");
        aCoordinateLocation = GLES20.glGetAttribLocation(program, "aCoordinate");
        uSamplerLocation = GLES20.glGetUniformLocation(program, "uSampler");

        isCreate = true;
    }

    @Override
    public void onChange(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);

        if (isBindFbo) {
            int[] fboData = OpenGLESUtils.getFbo(width, height);
            fboId = fboData[0];
            fboTextureId = fboData[1];
        }

        isChange = true;
    }

    protected void f(){

    }

    @Override
    public void onDraw(int textureId) {
        this.textureId = textureId;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        if (isBindFbo) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fboTextureId, 0);
            GLES20.glViewport(0, 0, width, height);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uSamplerLocation, 0);

        f();

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
