package com.demo.opengles.world.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.demo.opengles.R;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.world.MatrixHelper;
import com.demo.opengles.world.base.WorldObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class TexturePlane extends WorldObject {

    private final String vertexShaderCode =
            "uniform mat4 uMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aCoordinate;\n" +
                    "varying vec2 vCoordinate;\n" +
                    "void main(){\n" +
                    "    gl_Position=uMatrix*aPosition;\n" +
                    "    vCoordinate=aCoordinate;\n" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D uTexture;\n" +
                    "varying vec2 vCoordinate;\n" +
                    "void main(){\n" +
                    "    vec4 textColor=texture2D(uTexture,vCoordinate);" +
                    "    gl_FragColor=textColor;\n" +
                    "}";

    //顶点坐标
    private final float[] vertexCoords = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    //纹理坐标
    private final float[] textureCoord = {
            0.0f, 0.0f, //左上、原点
            0.0f, 1.0f, //左下
            1.0f, 0.0f, //右上
            1.0f, 1.0f, //右下
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    private int mProgram;

    private int glUMatrix;
    private int glAPosition;
    private int glACoordinate;
    private int glUTexture;

    private static Bitmap textureBmp;
    private int textureId;

    private static int COORDS_PER_VERTEX = 2; //每个顶点有2个float数字表示其坐标
    private final int vertexCount = vertexCoords.length / COORDS_PER_VERTEX; //顶点个数
    private final int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的步长， 每个float四个字节

    public TexturePlane(Context context) {
        super(context);
    }

    public void create() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(textureCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        textureCoordBuffer = cc.asFloatBuffer();
        textureCoordBuffer.put(textureCoord);
        textureCoordBuffer.position(0);

        vertexShaderIns = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShaderIns = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderIns);
        GLES20.glAttachShader(mProgram, fragmentShaderIns);
        GLES20.glLinkProgram(mProgram);

        glUMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        glAPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        glACoordinate = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        glUTexture = GLES20.glGetUniformLocation(mProgram, "uTexture");

        if (textureBmp == null) {
            textureBmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.flat);
        }
        textureId = OpenGLESUtil.createBitmapTextureId(textureBmp, GLES20.GL_TEXTURE0);
    }

    public void change(GL10 gl, int width, int height) {

    }

    public void draw(float[] MVPMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getWorldMatrix());
        GLES20.glUniformMatrix4fv(glUMatrix, 1, false, effectMatrix, 0);

        GLES20.glEnableVertexAttribArray(glAPosition);
        GLES20.glVertexAttribPointer(glAPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(glUTexture, 0);

        GLES20.glEnableVertexAttribArray(glACoordinate);
        GLES20.glVertexAttribPointer(glACoordinate, 2, GLES20.GL_FLOAT, false,
                vertexStride, textureCoordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
    }

}
