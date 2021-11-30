package com.demo.opengles.light;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.demo.opengles.util.MathUtil;
import com.demo.opengles.util.OpenGLESUtil;
import com.demo.opengles.world.MatrixHelper;
import com.demo.opengles.world.base.WorldObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * 漫反射光立方体
 *
 * 定义：
 * 不同的表面会以不同的方式反射光。镜面会将光线以与入射光相同角度的反方向反射出去。漫反射表面则会将入射光均等地反射到各个方向。
 *
 * 特征：
 * 漫反射光依赖于光源的方向。漫反射光在物体朝向光源的一面才有光照效果，在背面则没有光照效果
 */
public class DiffuseLightCube extends WorldObject {

    private final String vertexShaderCode =
            "uniform mat4 uMatrix;" +
                    "uniform vec3 uLightColor;" + //光源的颜色
                    "uniform float uLightStrong;" + //光源的强度
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "varying vec3 ambient;" + //光的实际生效值
                    "void main() {" +
                    "  gl_Position = uMatrix*aPosition;" +
                    "  vColor = aColor;" +
                    "  float ambientStrength = uLightStrong;" +
                    "  ambient = ambientStrength * uLightColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "varying vec3 ambient;" +
                    "void main() {" +
                    "  vec3 finalColor = ambient * vec3(vColor);" +
                    "  gl_FragColor = min(vec4(finalColor, vColor.a), vec4(1.0));" +
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

    /**
     * 各个顶点的法向量
     */
    final short normalCoords[] = {

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
    private int mLightColorHandler;
    private int mLightStrongHandler;
    private int mPositionHandle;
    private int mColorHandle;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    public Bitmap textureBmp;

    //环境光的强度
    private float lightStrong = 0.3f;

    private int COORDS_PER_VERTEX = 3;  //每个顶点有3个数字来表示它的坐标
    private int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容
    private int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的坐标有3个数值，数值都是float类型，每个float
    private int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

    public void setLightStrong(float lightStrong) {
        lightStrong = MathUtil.clamp(lightStrong, 0f, 1f);
        this.lightStrong = lightStrong;
    }

    public DiffuseLightCube(Context context) {
        super(context);
    }

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

        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        mLightColorHandler = GLES20.glGetUniformLocation(mProgram, "uLightColor");
        mLightStrongHandler = GLES20.glGetUniformLocation(mProgram, "uLightStrong");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    public void change(GL10 gl, int width, int height) {
    }

    public void draw(float[] MVPMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getWorldMatrix());
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, effectMatrix, 0);

        GLES20.glEnableVertexAttribArray(mLightColorHandler);
        //设置环境光的颜色（设置环境光的颜色，可以近似认为处于一个房间内，房间的四面墙壁、天花板、地板都涂有此颜色的涂料）
        GLES20.glUniform3f(mLightColorHandler, 1.0f, 1.0f, 1.0f);

        GLES20.glUniform1f(mLightStrongHandler, lightStrong);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        //用索引法来绘制三角形，最后这些三角形就会组合成一个正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT
                , indexBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
