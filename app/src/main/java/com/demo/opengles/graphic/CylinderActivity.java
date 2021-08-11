package com.demo.opengles.graphic;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CylinderActivity extends AppCompatActivity {

    private FloatBuffer vertexBuffer;  //柱面顶点buffer
    private final String vertexShaderCode =
            "uniform mat4 vMatrix;" +
                    "varying vec4 vColor;\n" +
                    "attribute vec4 vPosition;\n" +
                    "uniform vec4 vColorCenter;\n" +
                    "void main(){\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    if(vPosition.z >=0.0){\n" +
                    "        vColor=vColorCenter;\n" +
                    "    }else{\n" +
                    "        vColor=vec4(1.0,0.0,0.0,1.0);\n" +
                    "    }\n" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;

    static final int COORDS_PER_VERTEX = 3;

    private int mPositionHandle;
    private int mColorHandle;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    private int mMatrixHandler;
    private int mVColorCenterHandler;

    private float radius = 1f;
    private int n = 360;  //切割份数

    private float[] shapePos;

    private float height = 2.0f;

    //设置圆柱中心顶点的颜色值
    float centerColor[] = {0.0f, 1.0f, 0.0f, 1.0f};

    //柱面顶点坐标数组，每个顶点都是三维坐标
    private float[] createPositions() {
        ArrayList<Float> pos = new ArrayList<>();
        float angDegSpan = 360f / n;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            pos.add((float) (radius * Math.sin(i * Math.PI / 180f)));
            pos.add((float) (radius * Math.cos(i * Math.PI / 180f)));
            pos.add(height);
            pos.add((float) (radius * Math.sin(i * Math.PI / 180f)));
            pos.add((float) (radius * Math.cos(i * Math.PI / 180f)));
            pos.add(-height);
        }
        float[] d = new float[pos.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = pos.get(i);
        }
        return d;
    }

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
                //将背景设置为灰色
                GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

                shapePos = createPositions();
                ByteBuffer bb = ByteBuffer.allocateDirect(shapePos.length * 4);
                bb.order(ByteOrder.nativeOrder());

                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(shapePos);
                vertexBuffer.position(0);

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
                //计算宽高比
                float ratio = (float) width / height;
                //设置透视投影
                Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
                //设置相机位置
                Matrix.setLookAtM(mViewMatrix, 0, 1.0f, -10.0f, -4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                //计算变换矩阵
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //用onCreate中通过GLES20.glClearColor指定的颜色来刷新缓冲区
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                //将程序加入到OpenGLES2.0环境
                GLES20.glUseProgram(mProgram);

                //获取变换矩阵vMatrix成员句柄
                mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
                //指定vMatrix的值
                GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);

                mVColorCenterHandler = GLES20.glGetUniformLocation(mProgram, "vColorCenter");
                GLES20.glUniform4fv(mVColorCenterHandler, 1, centerColor, 0);

                //获取顶点着色器的vPosition成员句柄
                mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                //启用三角形顶点的句柄
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                //准备三角形的坐标数据
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer);

                //获取片元着色器的vColor成员的句柄
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
                //设置绘制三角形的颜色
                GLES20.glUniform4fv(mColorHandle, 1, centerColor, 0);

                //绘制三角形
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, shapePos.length / 3);

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
