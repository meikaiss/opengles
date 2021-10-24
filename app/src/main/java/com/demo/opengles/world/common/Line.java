package com.demo.opengles.world.common;

import android.content.Context;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.world.MatrixHelper;
import com.demo.opengles.world.base.WorldObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Line extends WorldObject {

    private final String vertexShaderCode =
            "uniform mat4 uMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying  vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = uMatrix*aPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    //顶点坐标
    private int COORDS_PER_VERTEX = 3;
    private float[] vertexCoords = {
            0f, 0f, 0f,    //起点
            1f, 0f, 0f  //终点
    };

    private int COORDS_PER_COLOR = 4;
    private float[] color = {
            1f, 0f, 0f, 1f, //起点颜色
            0f, 1f, 0f, 1f, //终点颜色
    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    private int mProgram;

    private int mMatrixHandler;
    private int mPositionHandle;
    private int mColorHandle;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    private int vertexStride = COORDS_PER_VERTEX * MatrixHelper.FLOAT_SIZE;
    private int colorStride = COORDS_PER_COLOR * MatrixHelper.FLOAT_SIZE;


    public Line(Context context) {
        super(context);
    }

    public void setVertexCoord(float startX, float startY, float startZ,
                               float endX, float endY, float endZ) {
        vertexCoords = new float[]{startX, startY, startZ, endX, endY, endZ};
    }

    public void setColor(float startR, float startG, float startB, float startA,
                         float endR, float endG, float endB, float endA) {
        color = new float[]{startR, startG, startB, startA, endR, endG, endB, endA};
    }

    @Override
    public void create() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        vertexShaderIns = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShaderIns = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderIns);
        GLES20.glAttachShader(mProgram, fragmentShaderIns);
        GLES20.glLinkProgram(mProgram);

        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    @Override
    public void change(GL10 gl, int width, int height) {

    }

    @Override
    public void draw(float[] MVPMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getWorldMatrix());
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, effectMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
