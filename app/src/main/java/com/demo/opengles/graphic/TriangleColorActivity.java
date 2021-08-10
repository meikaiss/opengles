package com.demo.opengles.graphic;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleColorActivity extends AppCompatActivity {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;
    private FloatBuffer vertexBuffer, colorBuffer;

    static final int COORDS_PER_VERTEX = 4;  //每个顶点有4个数字来表示它的坐标
    static final int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容

    /**
     * 每个坐标用4个float表示，xyz分别表示横纵Z坐标范围[-1.0f,-1.0f]。
     * 第四维目前感觉应该表示与人眼的距离，1.0f表示默认的标准距离，那么0.5f就表示人眼与图形的距离是标准距离的一半，那么相应的效果等价于这一顶点上xyz坐标的数值放大1倍；类似的2
     * .0f就表示人眼与图形的距离是标准距离的两倍，那么等价于这一顶点上的xyz数值缩小为原来的一半。
     */
    static float triangleCoords[] = {
            0.0f, 0.5f, 0.0f, 1f, // top
            -0.5f, -0.5f, 0.0f, 1.0f, // bottom left
            0.5f, -0.5f, 0.0f, 1.0f  // bottom right
    };

    private int mPositionHandle;
    private int mColorHandle;

    //顶点个数
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量，即每一个顶点所占用的字节大小，每个顶点的坐标有3个float数字，所以为3*4
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个float四个字节
    private final int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

    //设置颜色，依次为红绿蓝和透明通道
    float color[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //将背景设置为灰色
                GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

                ByteBuffer bb = ByteBuffer.allocateDirect(
                        triangleCoords.length * 4); //其中4的来源是因为每一个float占用4个字节
                bb.order(ByteOrder.nativeOrder());
                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(triangleCoords);
                vertexBuffer.position(0);

                ByteBuffer dd = ByteBuffer.allocateDirect(
                        color.length * 4);
                dd.order(ByteOrder.nativeOrder());
                colorBuffer = dd.asFloatBuffer();
                colorBuffer.put(color);
                colorBuffer.position(0);

                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                        vertexShaderCode);
                int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                        fragmentShaderCode);

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
                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //用onCreate中通过GLES20.glClearColor指定的颜色来刷新缓冲区
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                //将程序加入到OpenGLES2.0环境
                GLES20.glUseProgram(mProgram);

                //获取顶点着色器的vPosition成员句柄
                mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                //启用三角形顶点的句柄
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                //准备三角形的坐标数据
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer);

                //获取片元着色器的vColor成员的句柄
                mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
                //设置绘制三角形的颜色
                GLES20.glEnableVertexAttribArray(mColorHandle);
                GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false,
                        colorStride, colorBuffer);

                //绘制三角形
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
                //禁止顶点数组的句柄
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                GLES20.glDisableVertexAttribArray(mColorHandle);
            }
        });

        //必须在setRenderer之后才能调用
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setContentView(glSurfaceView);
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
