package com.demo.opengles.graphic;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.main.BaseActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CubeActivity extends BaseActivity {

    private FloatBuffer vertexBuffer, colorBuffer;
    private ShortBuffer indexBuffer;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;

    static final int COORDS_PER_VERTEX = 3;  //每个顶点有3个数字来表示它的坐标
    static final int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容

    //每个顶点的坐标，z轴坐标需要启用openGL深度功能
    final float cubePositions[] = {
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
            0f, 0f, 0f, 1f,
            1f, 1f, 1f, 1f,
    };

    private int mPositionHandle;
    private int mColorHandle;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int mMatrixHandler;

    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的坐标有3个数值，数值都是float类型，每个float
    private final int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节
    // 类型占4个字节

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        setContentView(glSurfaceView);

        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //开启深度测试
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);

                ByteBuffer bb = ByteBuffer.allocateDirect(cubePositions.length * 4);
                bb.order(ByteOrder.nativeOrder());
                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(cubePositions);
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

                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
                int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
                //创建一个空的OpenGLES程序
                mProgram = GLES20.glCreateProgram();
                //将顶点着色器加入到程序
                GLES20.glAttachShader(mProgram, vertexShader);
                //将片元着色器加入到程序中
                GLES20.glAttachShader(mProgram, fragmentShader);
                //连接到着色器程序
                GLES20.glLinkProgram(mProgram);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                //计算宽高比
                float ratio = (float) width / height;
                //设置透视投影
                Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
                //设置相机位置
                Matrix.setLookAtM(mViewMatrix, 0, 5.0f, 5.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                //计算变换矩阵
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                //将程序加入到OpenGLES2.0环境
                GLES20.glUseProgram(mProgram);

                //获取变换矩阵vMatrix成员句柄
                mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
                //指定vMatrix的值
                GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);

                //获取顶点着色器的vPosition成员句柄
                mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                //启用三角形顶点的句柄
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                //准备三角形的坐标数据
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

                //获取片元着色器的vColor成员的句柄
                mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
                GLES20.glEnableVertexAttribArray(mColorHandle);
                GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                        GLES20.GL_FLOAT, false, colorStride, colorBuffer);

                //用索引法来绘制三角形，最张这些三角形就会组合成一个正方体
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT
                        , indexBuffer);
                //禁止顶点数组的句柄
                GLES20.glDisableVertexAttribArray(mPositionHandle);
            }
        });

        //必须在setRenderer之后才能调用
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public int loadShader(int type, String shaderCode) {
        //根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
