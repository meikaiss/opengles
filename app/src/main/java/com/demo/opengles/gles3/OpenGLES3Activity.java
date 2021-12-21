package com.demo.opengles.gles3;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLES3Activity extends BaseActivity {

    static final int COORDS_PER_VERTEX = 4;  //每个顶点有4个数字来表示它的坐标
    static final int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量，即每一个顶点所占用的字节大小，每个顶点的坐标有3个float数字，所以为3*4
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个float四个字节
    private final int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

    private static float triangleCoords[] = {
            0.0f, 0.5f, 0.0f, 1f, // top
            -0.5f, -0.5f, 0.0f, 1.0f, // bottom left
            0.5f, -0.5f, 0.0f, 1.0f  // bottom right
    };

    //设置颜色，依次为红绿蓝和透明通道
    private static float color[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };

    private int mProgram;

    private int vertexShaderIns;
    private int fragmentShaderIns;

    private int aPosition;
    private int aColor;

    private FloatBuffer vertexBuffer, colorBuffer;

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        setContentView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(3);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES30.glClearColor(1f, 0f, 0f, 1.0f);

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

                vertexShaderIns = OpenGLESUtil.loadShader3(GLES30.GL_VERTEX_SHADER,
                        OpenGLESUtil.getShaderCode(context, "shader/gles3/vertex.glsl"));
                fragmentShaderIns = OpenGLESUtil.loadShader3(GLES30.GL_FRAGMENT_SHADER,
                        OpenGLESUtil.getShaderCode(context, "shader/gles3/fragment.glsl"));

                mProgram = GLES30.glCreateProgram();
                GLES30.glAttachShader(mProgram, vertexShaderIns);
                GLES30.glAttachShader(mProgram, fragmentShaderIns);
                GLES30.glLinkProgram(mProgram);

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES30.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

                GLES30.glUseProgram(mProgram);

                GLES30.glEnableVertexAttribArray(aPosition);
                GLES30.glVertexAttribPointer(aPosition, COORDS_PER_VERTEX,
                        GLES30.GL_FLOAT, false,
                        vertexStride, vertexBuffer);

                GLES30.glEnableVertexAttribArray(aColor);
                GLES30.glVertexAttribPointer(aColor, COORDS_PER_COLOR, GLES30.GL_FLOAT, false,
                        colorStride, colorBuffer);


                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
                GLES30.glDisableVertexAttribArray(aPosition);
                GLES30.glDisableVertexAttribArray(aColor);
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}