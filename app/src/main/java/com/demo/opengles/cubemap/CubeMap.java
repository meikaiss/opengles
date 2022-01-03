package com.demo.opengles.cubemap;

import android.content.Context;
import android.opengl.GLES20;

import com.demo.opengles.gaussian.render.BaseRenderObject;
import com.demo.opengles.util.OpenGLESUtil;

public class CubeMap extends BaseRenderObject {

    private float[] cubeVertices = {
            // positions          // texture Coords
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    };

    /**
     * 立方体的8个顶点
     */
    private float[] skyboxVertices = new float[]{
            -1f, 1f, 1f, // 上左前顶点
            1f, 1f, 1f, // 上右前顶点
            -1f, 1f, -1f, // 上左后顶点
            1f, 1f, -1f, // 上右后顶点

            -1f, -1f, 1f, // 下左前顶点
            1f, -1f, 1f, // 下右前顶点
            -1f, -1f, -1f, // 下左后顶点
            1f, -1f, -1f, // 下右后顶点
    };
    // 立方体索引
    private static final short[] skyboxIndex = new short[]{
            // Front
            1, 3, 0,
            0, 3, 2,

            // Back
            4, 6, 5,
            5, 6, 7,

            // Left
            0, 2, 4,
            4, 2, 6,

            // Right
            5, 7, 1,
            1, 7, 3,

            // Top
            5, 1, 4,
            4, 1, 0,

            // Bottom
            6, 2, 7,
            7, 2, 3
    };


    private int vertexShaderIns;
    private int fragmentShaderIns;

    private int mProgram;

    private int shaderProgram, positionHandle, mMVPMatrixHandle, skyBoxPosHandle;

    public CubeMap(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initShaderFileName("shader/cubemap/cube_map_vertex.glsl", "shader/cubemap/cube_map_fragment.glsl");

        vertexShaderIns = OpenGLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShaderIns = OpenGLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderIns);
        GLES20.glAttachShader(mProgram, fragmentShaderIns);
        GLES20.glLinkProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        skyBoxPosHandle = GLES20.glGetUniformLocation(shaderProgram, "skybox");
    }

    @Override
    public void onChange(int width, int height) {
        super.onChange(width, height);
    }

    @Override
    public void onDraw(int textureId) {
        super.onDraw(textureId);


    }

}
