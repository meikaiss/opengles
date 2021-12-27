package com.demo.opengles.gles3;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;

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
                GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

                vertexBuffer = OpenGLESUtil.createFloatBuffer(triangleCoords);
                colorBuffer = OpenGLESUtil.createFloatBuffer(color);

                vertexShaderIns = OpenGLESUtil.loadShader3(GLES30.GL_VERTEX_SHADER,
                        OpenGLESUtil.getShaderCode(context, "shader/gles3/vertex.glsl"));
                fragmentShaderIns = OpenGLESUtil.loadShader3(GLES30.GL_FRAGMENT_SHADER,
                        OpenGLESUtil.getShaderCode(context, "shader/gles3/fragment.glsl"));

                mProgram = GLES30.glCreateProgram();
                GLES30.glAttachShader(mProgram, vertexShaderIns);
                GLES30.glAttachShader(mProgram, fragmentShaderIns);
                GLES30.glLinkProgram(mProgram);

                aPosition = GLES30.glGetAttribLocation(mProgram, "aPosition");
                aColor = GLES30.glGetAttribLocation(mProgram, "aColor");
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
                /**
                 * 在opengles3中不再需要先获取句柄，然后通过句柄操作，而直接通过shader中的location值来操作
                 * 当然，通过opengles2那种方式也仍然有效
                 */
                GLES30.glEnableVertexAttribArray(1);

                GLES30.glVertexAttribPointer(aPosition, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false,
                        vertexStride, vertexBuffer);
                GLES30.glVertexAttribPointer(1, COORDS_PER_COLOR, GLES30.GL_FLOAT, false,
                        colorStride, colorBuffer);

                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);

                GLES30.glDisableVertexAttribArray(aPosition);
                GLES30.glDisableVertexAttribArray(1);
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}