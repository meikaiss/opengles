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

import javax.microedition.khronos.opengles.GL10;

/**
 * 镜面反射光立方体
 * <p>
 * 定义：
 * 镜面反射的结果是物体在从某个角度看上去会十分明亮，而移动开后这个光亮又会消失
 * <p>
 * 特征：
 * 现实中好的镜面反射的例子是金属物体，这些物体有时候看上去由于太亮了导致看不到他本来的颜色而是直接照向你眼睛的白色的亮光。
 * 但这种属性在其他的一些材料上是没有的（比如：木头）。很多东西根本不会发光，不管光源从什么角度照射以及观察者在什么位置。所以，镜面反射光的存在更取决于反射物体的材料性质而不是光源本身。
 */
public class SpecularLightCube extends WorldObject {

    //顶点坐标（xyz），以散列三角形方式绘制
    final float vertexCoords[] = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
    };

    /**
     * 各个顶点的法向量
     * 因为此例中我们只绘制一个正方形，因此各个顶点的法向量都是Z轴正方向
     */
    final float normalCoords[] = {
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
    };

    //4个顶点的颜色，与顶点坐标一一对应
    float color[] = {
            1f, 0f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 0f, 1f, 1f,
            0f, 0f, 1f, 1f,
            1f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer colorBuffer;

    private int mProgram;

    private int mMatrixHandler;
    private int mModelMatrixHandler;
    private int mLightColorHandler;
    private int mLightPosHandler;
    private int mViewPosHandler;
    private int mSpecularStrongHandler;

    private int mPositionHandle;
    private int mNormalHandle;
    private int mColorHandle;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    public Bitmap textureBmp;

    //漫反射光的强度
    private float lightStrong = 0.8f;
    //光源xy坐标
    private float lightPosXY = 50;

    private int COORDS_PER_VERTEX = 3;  //每个顶点有3个数字来表示它的坐标
    private int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容
    private int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的坐标有3个数值，数值都是float类型，每个float
    private int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

    public void setLightStrong(float lightStrong) {
        lightStrong = MathUtil.clamp(lightStrong, 0f, 1f);
        this.lightStrong = lightStrong;
    }

    public void setLightPosXY(float lightPosXY) {
        this.lightPosXY = lightPosXY;
    }

    public SpecularLightCube(Context context) {
        super(context);
    }

    public void create() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        ByteBuffer bbNormal = ByteBuffer.allocateDirect(normalCoords.length * 4);
        bbNormal.order(ByteOrder.nativeOrder());
        normalBuffer = bbNormal.asFloatBuffer();
        normalBuffer.put(normalCoords);
        normalBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        vertexShaderIns = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER,
                OpenGLESUtil.getShaderCode(context, "shader/light/specular/specular_vertex.glsl"));
        fragmentShaderIns = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER,
                OpenGLESUtil.getShaderCode(context, "shader/light/specular/specular_fragment.glsl"));

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderIns);
        GLES20.glAttachShader(mProgram, fragmentShaderIns);
        GLES20.glLinkProgram(mProgram);

        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        mModelMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uModelMatrix");
        mLightColorHandler = GLES20.glGetUniformLocation(mProgram, "uLightColor");
        mLightPosHandler = GLES20.glGetUniformLocation(mProgram, "uLightPos");
        mViewPosHandler = GLES20.glGetUniformLocation(mProgram, "uViewPosLoc");
        mSpecularStrongHandler = GLES20.glGetUniformLocation(mProgram, "uSpecularStrength");

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    public void change(GL10 gl, int width, int height) {
    }

    public void draw(float[] MVPMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] effectMatrix = MatrixHelper.multiplyMM(MVPMatrix, getModelMatrix());
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, effectMatrix, 0);

        GLES20.glUniformMatrix4fv(mModelMatrixHandler, 1, false, getModelMatrix(), 0);

        GLES20.glEnableVertexAttribArray(mLightColorHandler);
        GLES20.glUniform3f(mLightColorHandler, 1.0f, 1.0f, 1.0f);

        float testValueXY = lightPosXY;
        float testValueZ = 20f;
        //设置镜面反射光源位置
        GLES20.glUniform3f(mLightPosHandler, testValueXY, testValueXY, testValueZ);
        //设置观察点的位置（必须与相机位置相同，才能得到符合物理世界规律的图像）
        GLES20.glUniform3f(mViewPosHandler, -20, -20, 20);

        /**
         * 外部设置了specularCube.setScale(10f, 10f, 10f);来放大10倍
         * 同时镜面反射原理中又用到 pow 函数增加镜面效果
         *
         * 因此当 testValueXY 值较小时，镜面反射的光几乎无法射到人眼位置
         *
         */
        GLES20.glUniform1f(mSpecularStrongHandler, lightStrong);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, normalBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        /**
         * 参数1：GL_TRIANGLES，表示按顶点顺序，每3个顶点绘制一个三角形，并且不共用顶点
         * 参数2：0，表示起始顶点的索引（在本例中每个顶点使用了3个float来表示x、y、z）
         * 参数3：6，表示从索引指定的顶点开始，往后连续采用6个顶点
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
