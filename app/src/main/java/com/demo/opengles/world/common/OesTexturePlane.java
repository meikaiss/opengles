package com.demo.opengles.world.common;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;

import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.world.MatrixHelper;
import com.demo.opengles.world.base.WorldObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class OesTexturePlane extends WorldObject {

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
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES uSampler;\n" +
                    "varying vec2 vCoordinate;\n" +
                    "void main(){\n" +
                    "    vec4 textColor=texture2D(uSampler,vCoordinate);" +
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

    private int oesTextureId;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private static int COORDS_PER_VERTEX = 2; //每个顶点有2个float数字表示其坐标
    private final int vertexCount = vertexCoords.length / COORDS_PER_VERTEX; //顶点个数
    private final int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的步长， 每个float四个字节

    private OnSurfacePrepareListener onSurfacePrepareListener;

    public void setOnSurfacePrepareListener(OnSurfacePrepareListener onSurfacePrepareListener) {
        this.onSurfacePrepareListener = onSurfacePrepareListener;
    }

    public OesTexturePlane(Context context) {
        super(context);
    }

    public interface OnSurfacePrepareListener {
        void onSurfacePrepare(Surface surface);
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
        glUTexture = GLES20.glGetUniformLocation(mProgram, "uSampler");

        oesTextureId = OpenGLESUtil.createOesTexture();
        surfaceTexture = new SurfaceTexture(oesTextureId);
        surface = new Surface(surfaceTexture);

        if (onSurfacePrepareListener != null) {
            onSurfacePrepareListener.onSurfacePrepare(surface);
        }
    }

    public void change(GL10 gl, int width, int height) {

    }

    public void draw(float[] MVPMatrix) {
        surfaceTexture.updateTexImage();

        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getModelMatrix());
        GLES20.glUniformMatrix4fv(glUMatrix, 1, false, effectMatrix, 0);

        GLES20.glEnableVertexAttribArray(glAPosition);
        GLES20.glVertexAttribPointer(glAPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);

        GLES20.glEnableVertexAttribArray(glACoordinate);
        GLES20.glVertexAttribPointer(glACoordinate, 2, GLES20.GL_FLOAT, false,
                vertexStride, textureCoordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
    }

}
