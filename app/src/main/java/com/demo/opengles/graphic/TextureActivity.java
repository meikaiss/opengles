package com.demo.opengles.graphic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureActivity extends AppCompatActivity {

    private final String vertexShaderCode =
            "uniform mat4 vMatrix;\n" +
                    "attribute vec4 vPosition;\n" +
                    "attribute vec2 vCoordinate;\n" +
                    "varying vec2 aCoordinate;\n" +
                    "void main(){\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    aCoordinate=vCoordinate;\n" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D vTexture;\n" +
                    "varying vec2 aCoordinate;\n" +
                    "void main(){\n" +
                    "    gl_FragColor=texture2D(vTexture,aCoordinate);\n" +
                    "}";

    private int mProgram;

    //顶点坐标
    private final float[] vertexCoords_1 = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    //顶点坐标-显示在中心区域
    private final float[] vertexCoords_2 = {
            -0.5f, 0.5f,    //左上角
            -0.5f, -0.5f,   //左下角
            0.5f, 0.5f,     //右上角
            0.5f, -0.5f     //右下角
    };

    private float[] vertexCoords = vertexCoords_1;

    //纹理坐标-正放图片，纹理坐标与顶点坐标出现的顺序完全相同，则可以呈现出正放的图片
    private final float[] textureCoord_1 = {
            0.0f, 0.0f, //左上、原点
            0.0f, 1.0f, //左下
            1.0f, 0.0f, //右上
            1.0f, 1.0f, //右下
    };

    //纹理坐标-上下颠倒图片，纹理坐标与顶点坐标的上下顺序颠倒，则可以呈现上下颠倒的图片
    private final float[] textureCoord_2 = {
            0.0f, 1.0f, //左下
            0.0f, 0.0f, //左上、原点
            1.0f, 1.0f, //右下
            1.0f, 0.0f, //右上
    };

    //纹理坐标-左右翻转图片，纹理坐标与顶点坐标的左右翻转颠倒，则可以呈现上下翻转的图片
    private final float[] textureCoord_3 = {
            1.0f, 0.0f, //右上
            1.0f, 1.0f, //右下
            0.0f, 0.0f, //左上、原点
            0.0f, 1.0f, //左下
    };

    //纹理坐标-围绕中心旋转180度图片，顶点坐标的三角形顺序与纹理坐标的采样顺序左右、上下均倒置，即等同于旋转180度
    private final float[] textureCoord_4 = {
            1.0f, 1.0f, //右下
            1.0f, 0.0f, //右上
            0.0f, 1.0f, //左下
            0.0f, 0.0f, //左上、原点
    };

    private final float[] textureCoord_5 = {
            0.25f, 0.25f, //左上、原点
            0.25f, 0.75f, //左下
            0.75f, 0.25f, //右上
            0.75f, 0.75f, //右下
    };

    private float[] textureCoord = textureCoord_1;

    private int glVPosition;
    private int glVTexture;
    private int glVCoordinate;
    private int glVMatrix;

    private int textureId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordBuffer;

    private static int COORDS_PER_VERTEX = 2;
    //顶点个数
    private final int vertexCount = vertexCoords.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量，即每一个顶点所占用的字节大小，每个顶点的坐标有3个float数字，所以为3*4
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个float四个字节

    private Bitmap textureBmp;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private GLSurfaceView glSurfaceView;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_1) {
                textureCoord = textureCoord_1;
            } else if (v.getId() == R.id.btn_2) {
                textureCoord = textureCoord_2;
            } else if (v.getId() == R.id.btn_3) {
                textureCoord = textureCoord_3;
            } else if (v.getId() == R.id.btn_4) {
                textureCoord = textureCoord_4;
            } else if (v.getId() == R.id.btn_5) {
                vertexCoords = vertexCoords_2;
                textureCoord = textureCoord_1;
            } else if (v.getId() == R.id.btn_6) {
                vertexCoords = vertexCoords_1;
                textureCoord = textureCoord_5;
            }
            glSurfaceView.requestRender();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        glSurfaceView = findViewById(R.id.gl_surface_view);

        textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image);
        glSurfaceView.setEGLContextClientVersion(2);

        findViewById(R.id.btn_1).setOnClickListener(onClickListener);
        findViewById(R.id.btn_2).setOnClickListener(onClickListener);
        findViewById(R.id.btn_3).setOnClickListener(onClickListener);
        findViewById(R.id.btn_4).setOnClickListener(onClickListener);
        findViewById(R.id.btn_5).setOnClickListener(onClickListener);
        findViewById(R.id.btn_6).setOnClickListener(onClickListener);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //rgb=0.4表示背景为灰色
                GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
                //启用2d纹理功能，包含2d采样
                GLES20.glEnable(GLES20.GL_TEXTURE_2D);

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

                //透视变换与眼睛观察位置的 综合矩阵
                glVMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
                //获取句柄，用于将内存中的顶点坐标传递给GPU
                glVPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
                //获取句柄，用于将内存中的纹理坐标传递给GPU
                glVCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
                glVTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");

                //将显卡中的第0号纹理单元 赋值给 纹理句柄
                GLES20.glUniform1i(glVTexture, 0);

                textureId = createTexture();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);

                int w = textureBmp.getWidth();
                int h = textureBmp.getHeight();
                float sWH = w / (float) h;
                float sWidthHeight = width / (float) height;
                if (width > height) {
                    if (sWH > sWidthHeight) {
                        Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH,
                                -1, 1, 3, 7);
                    } else {
                        Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH,
                                -1, 1, 3, 7);
                    }
                } else {
                    if (sWH > sWidthHeight) {
                        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH,
                                1 / sWidthHeight * sWH, 3, 7);
                    } else {
                        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight,
                                sWH / sWidthHeight, 3, 7);
                    }
                }
                //设置相机位置
                Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                //计算变换矩阵
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //将内存中的顶点坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
                ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
                bb.order(ByteOrder.nativeOrder());
                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(vertexCoords);
                vertexBuffer.position(0);

                //将内存中的纹理坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
                ByteBuffer cc = ByteBuffer.allocateDirect(textureCoord.length * 4);
                cc.order(ByteOrder.nativeOrder());
                textureCoordBuffer = cc.asFloatBuffer();
                textureCoordBuffer.put(textureCoord);
                textureCoordBuffer.position(0);

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                GLES20.glUseProgram(mProgram);

                GLES20.glUniformMatrix4fv(glVMatrix, 1, false, mMVPMatrix, 0);

                GLES20.glEnableVertexAttribArray(glVPosition);
                GLES20.glVertexAttribPointer(glVPosition, 2, GLES20.GL_FLOAT, false, vertexStride
                        , vertexBuffer);

                GLES20.glEnableVertexAttribArray(glVCoordinate);
                GLES20.glVertexAttribPointer(glVCoordinate, 2, GLES20.GL_FLOAT, false,
                        vertexStride, textureCoordBuffer);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
            }

            private int createTexture() {
                int[] texture = new int[1];
                if (textureBmp != null && !textureBmp.isRecycled()) {
                    //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
                    GLES20.glGenTextures(1, texture, 0);
                    textureId = texture[0];
                    //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
                    //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST);
                    //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                            GLES20.GL_LINEAR);
                    //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                            GLES20.GL_CLAMP_TO_EDGE);
                    //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                            GLES20.GL_CLAMP_TO_EDGE);

                    //给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBmp, 0);

                    //返回生成的纹理的句柄
                    return texture[0];
                }
                return 0;
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
