package com.demo.opengles.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class Cube extends WorldObject {

    private final String vertexShaderCode =
            "uniform mat4 vMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying  vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*aPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    //每个顶点的坐标，z轴坐标需要启用openGL深度功能
    final float vertexCoords[] = {
            -1.0f, 1.0f, 1.0f,    //正面左上0
            -1.0f, -1.0f, 1.0f,   //正面左下1
            1.0f, -1.0f, 1.0f,    //正面右下2
            1.0f, 1.0f, 1.0f,     //正面右上3
            -1.0f, 1.0f, -1.0f,    //反面左上4
            -1.0f, -1.0f, -1.0f,   //反面左下5
            1.0f, -1.0f, -1.0f,    //反面右下6
            1.0f, 1.0f, -1.0f,     //反面右上7
    };

    /**
     * 顶点坐标的索引从0开始，674三个顶点表示一个三角形，674+645拼接起来就是正方体的背面
     */
    final short index[] = {
            6, 7, 4, 6, 4, 5,    //后面
            6, 3, 7, 6, 2, 3,    //右面
            6, 5, 1, 6, 1, 2,    //下面
            0, 3, 2, 0, 2, 1,    //正面
            0, 1, 5, 0, 5, 4,    //左面
            0, 7, 3, 0, 4, 7,    //上面
    };

    //八个顶点的颜色，与顶点坐标一一对应
    float color[] = {
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
            1f, 1f, 0f, 1f,
            1f, 0f, 1f, 1f,
            0f, 1f, 1f, 1f,
            0.5f, 0f, 1f, 1f,
            1f, 1f, 1f, 1f,
    };


    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;

    private int mProgram;

    private int mMatrixHandler;
    private int mPositionHandle;
    private int mColorHandle;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    public Bitmap textureBmp;

    public boolean isEffective;

    private int COORDS_PER_VERTEX = 3;  //每个顶点有3个数字来表示它的坐标
    private int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容
    private int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的坐标有3个数值，数值都是float类型，每个float
    private int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

    public Cube(Context context) {
        super(context);
    }

    void create() {
        //将内存中的顶点坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
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

        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2); //short类型占2个字节
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);

        vertexShaderIns = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShaderIns = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderIns);
        GLES20.glAttachShader(mProgram, fragmentShaderIns);
        GLES20.glLinkProgram(mProgram);

        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    void change(GL10 gl, int width, int height) {
    }

    void draw(float[] MVPMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getWorldMatrix());
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, effectMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        //用索引法来绘制三角形，最后这些三角形就会组合成一个正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT
                , indexBuffer);

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
