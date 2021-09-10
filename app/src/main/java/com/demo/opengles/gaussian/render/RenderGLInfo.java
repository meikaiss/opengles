package com.demo.opengles.gaussian.render;

import android.content.Context;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;

import java.nio.FloatBuffer;

public class RenderGLInfo {
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

    public float[] mViewMatrix = new float[16];
    public float[] mProjectMatrix = new float[16];
    public float[] mMVPMatrix = new float[16];

    public void initShaderFileName(String vertexFilename, String fragFilename) {
        this.vertexFilename = vertexFilename;
        this.fragFilename = fragFilename;
    }

    public void createProgram(Context context, boolean isBindFbo) {
        vertexShaderCode = OpenGLESUtil.getShaderCode(context, vertexFilename);
        fragShaderCode = OpenGLESUtil.getShaderCode(context, fragFilename);

        vertexBuffer = OpenGLESUtil.getSquareVertexBuffer();
        if (isBindFbo) {
            coordinateBuffer = OpenGLESUtil.getSquareCoordinateReverseBuffer();
        } else {
            coordinateBuffer = OpenGLESUtil.getSquareCoordinateBuffer();
        }
        vboId = OpenGLESUtil.getVbo(vertexBuffer, coordinateBuffer);

        vertexShader = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragShader = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderCode);

        program = OpenGLESUtil.linkProgram(vertexShader, fragShader);
    }

    public void createFBO(int width, int height) {
        int[] fboData = OpenGLESUtil.getFbo(width, height);
        fboId = fboData[0];
        fboTextureId = fboData[1];
    }

    public void release() {
        GLES20.glDeleteProgram(program);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragShader);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{vboId}, 0);

        GLES20.glDeleteTextures(1, new int[]{fboTextureId}, 0);
        GLES20.glDeleteFramebuffers(1, new int[]{fboId}, 0);
    }

}
