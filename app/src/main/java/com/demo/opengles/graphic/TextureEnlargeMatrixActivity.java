package com.demo.opengles.graphic;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;
import com.demo.opengles.util.CollectUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureEnlargeMatrixActivity extends AppCompatActivity {

    //放大镜效果-顶点着色器
    private final String vertexShaderCode =
            "uniform mat4 vMatrix;\n" +
                    "uniform vec4 enlargeParam;\n" +
                    "uniform vec4 enlargeVertexParam;\n" +
                    "\n" +
                    "attribute vec4 vPosition;\n" +
                    "attribute vec2 vCoordinate;\n" +
                    "\n" +
                    "varying vec2 aCoordinate;\n" +
                    "varying vec4 gPosition;\n" +
                    "varying vec4 vMEnlargeParam;\n" +
                    "varying vec4 vEnlargeParam;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    aCoordinate=vCoordinate;\n" +
                    "    gPosition=vMatrix*vPosition;\n" +
                    "    vMEnlargeParam=vMatrix*enlargeVertexParam;\n" +
                    "    vEnlargeParam=enlargeParam;\n" +
                    "}";

    //放大镜效果-片元着色器
    //同样一块纹理区域，原本纹理坐标是4-6的范围，现在却把2-3的范围的纹理图像填充到4-6范围里，自然会放大
    private final String fragmentShaderCode_1 =
            "precision mediump float;\n" +
                    "uniform sampler2D vTexture;\n" +
                    "uniform float uXY;\n" +
                    "\n" +
                    "varying vec2 aCoordinate;\n" +
                    "varying vec4 gPosition;\n" +
                    "varying vec4 vMEnlargeParam;\n" +
                    "varying vec4 vEnlargeParam;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    vec4 nColor=texture2D(vTexture,aCoordinate);\n" +
                    "    float dis=distance(vec2(gPosition.x,gPosition.y/uXY)," +
                    "                       vec2(vMEnlargeParam.r,vMEnlargeParam.g/uXY));\n" +
                    "    if(dis<0.5){\n" +
                    "        float deltaX=(vEnlargeParam.r-aCoordinate.x)*0.5;" +
                    "        float deltaY=(vEnlargeParam.g-aCoordinate.y)*0.5;" +
                    "        nColor=texture2D(vTexture,vec2(aCoordinate.x+deltaX," +
                    "                                       aCoordinate.y+deltaY));\n" +
                    "    }\n" +
                    "    gl_FragColor=nColor;\n" +
                    "}";

    //顶点坐标
    private final float[] vertexCoords_1 = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    //纹理坐标-正放图片，纹理坐标与顶点坐标出现的顺序完全相同，则可以呈现出正放的图片
    private final float[] textureCoord_1 = {
            0.0f, 0.0f, //左上、原点
            0.0f, 1.0f, //左下
            1.0f, 0.0f, //右上
            1.0f, 1.0f, //右下
    };

    private Bitmap textureBmp;

    private FrameLayout surfaceContainer;
    private GLSurfaceView glSurfaceView;
    private ColorRender render;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_enlarge_matrix);

        surfaceContainer = findViewById(R.id.surface_container);
        glSurfaceView = new GLSurfaceView(this);
        surfaceContainer.addView(glSurfaceView);

        textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image);
        glSurfaceView.setEGLContextClientVersion(2);

        render = new ColorRender();
        {
            RenderObject object1 = new RenderObject();
            object1.init(vertexShaderCode, fragmentShaderCode_1, vertexCoords_1, textureCoord_1,
                    textureBmp, true);
            render.renderObjectList.add(object1);
        }

        glSurfaceView.setRenderer(render);

        //必须在setRenderer之后才能调用
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int realBmpWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                int realBmpHeight = (int) ((float) textureBmp.getHeight() / textureBmp.getWidth()
                        * realBmpWidth);

                int eventX = (int) event.getX();
                int eventY = (int) event.getY();

                int viewWidth = glSurfaceView.getWidth();
                int viewHeight = glSurfaceView.getHeight();

                int realBmpTop = (viewHeight - realBmpHeight) / 2;
                int realBmpBottom = realBmpTop + realBmpHeight;

                eventY = Math.max(eventY, realBmpTop);
                eventY = Math.min(eventY, realBmpBottom);

                float x = (float) eventX / realBmpWidth;
                float y = (float) (eventY - realBmpTop) / realBmpHeight;

                Log.e("mk", "x=" + x + ",y=" + y);

                //手指点击位置的纹理坐标，点击位置即为放大的中心位置
                render.renderObjectList.get(0).enlargeTextureParam[0] = x;
                render.renderObjectList.get(0).enlargeTextureParam[1] = y;

                //手指点击位置的顶点坐标
                render.renderObjectList.get(0).enlargeVertexParam[0] = x * 2 - 1.0f;
                //sdk-View坐标系的y正方向向下，opengl世界坐标系的y正方向向上，因此转换时需要置负
                render.renderObjectList.get(0).enlargeVertexParam[1] = -(y * 2 - 1.0f);

                glSurfaceView.requestRender();
                return true;
            }
        });
    }

    private static class RenderObject {

        private String vertexShaderCode;
        private String fragmentShaderCode;

        private float[] vertexCoords;
        private float[] textureCoord;

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureCoordBuffer;

        private int mProgram;

        private int glVPosition;
        private int glVTexture;
        private int glVCoordinate;
        private int glVMatrix;
        private int glUXY;
        private int glEnlargeParam;
        private int glEnlargeVertexParam;

        private int textureId;

        private static int COORDS_PER_VERTEX = 2;
        //顶点个数
        private int vertexCount;
        //顶点之间的偏移量，即每一个顶点所占用的字节大小，每个顶点的坐标有3个float数字，所以为3*4
        private int vertexStride; // 每个float四个字节
        private float uXY; //视图宽高比

        private int vertexShaderIns;
        private int fragmentShaderIns;

        //放大的参数，第1、2个参数表示放大的中心点在世界坐标系中的坐标；第3,4个参数无意义，仅用于与透视矩阵4x4相乘
        private float[] enlargeVertexParam = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        private float[] enlargeTextureParam = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

        private float[] mViewMatrix = new float[16];
        private float[] mProjectMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];

        public Bitmap textureBmp;

        public boolean isEffective;

        public void init(String vertexShaderCode, String fragmentShaderCode,
                         float[] vertexCoords, float[] textureCoord,
                         Bitmap textureBmp, boolean isEffective) {
            this.vertexShaderCode = vertexShaderCode;
            this.fragmentShaderCode = fragmentShaderCode;
            this.vertexCoords = vertexCoords;
            this.textureCoord = textureCoord;
            this.textureBmp = textureBmp;
            this.isEffective = isEffective;

            vertexCount = vertexCoords.length / COORDS_PER_VERTEX;
            vertexStride = COORDS_PER_VERTEX * 4;
        }

        public void createProgram() {
            //rgb=0.4表示背景为灰色
            GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
            //启用2d纹理功能，包含2d采样
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);

            vertexShaderIns = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            fragmentShaderIns = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            //创建一个空的OpenGLES程序
            mProgram = GLES20.glCreateProgram();
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShaderIns);
            //将片元着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShaderIns);
            //连接到着色器程序
            GLES20.glLinkProgram(mProgram);

            //透视变换与眼睛观察位置的 综合矩阵
            glVMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
            //获取句柄，用于将内存中的顶点坐标传递给GPU
            glVPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
            //获取句柄，用于将内存中的纹理坐标传递给GPU
            glVCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
            glVTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
            glUXY = GLES20.glGetUniformLocation(mProgram, "uXY");
            glEnlargeParam = GLES20.glGetUniformLocation(mProgram, "enlargeParam");
            glEnlargeVertexParam = GLES20.glGetUniformLocation(mProgram, "enlargeVertexParam");
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

        public int loadShader(int type, String shaderCode) {
            //根据type创建顶点着色器或者片元着色器
            int shader = GLES20.glCreateShader(type);
            //将资源加入到着色器中，并编译
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        public void onDrawFrame(GL10 gl) {
            if (!isEffective) {
                return;
            }

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

            GLES20.glUniform1f(glUXY, uXY);
            GLES20.glUniform4fv(glEnlargeParam, 1, enlargeTextureParam, 0);
            GLES20.glUniform4fv(glEnlargeVertexParam, 1, enlargeVertexParam, 0);

            GLES20.glUniformMatrix4fv(glVMatrix, 1, false, mMVPMatrix, 0);

            GLES20.glEnableVertexAttribArray(glVPosition);
            GLES20.glVertexAttribPointer(glVPosition, 2, GLES20.GL_FLOAT, false, vertexStride
                    , vertexBuffer);

            GLES20.glEnableVertexAttribArray(glVCoordinate);
            GLES20.glVertexAttribPointer(glVCoordinate, 2, GLES20.GL_FLOAT, false,
                    vertexStride, textureCoordBuffer);

            //将显卡中的第0号纹理单元 赋值给 纹理句柄
            GLES20.glUniform1i(glVTexture, 0);
            textureId = createTexture();

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            int w = textureBmp.getWidth();
            int h = textureBmp.getHeight();
            float sWH_Bmp = w / (float) h;
            float sWH_View = width / (float) height;
            uXY = sWH_View;
            if (width > height) {
                if (sWH_Bmp > sWH_View) {
                    Matrix.orthoM(mProjectMatrix, 0, -sWH_View * sWH_Bmp, sWH_View * sWH_Bmp,
                            -1, 1, 3, 7);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -sWH_View / sWH_Bmp, sWH_View / sWH_Bmp,
                            -1, 1, 3, 7);
                }
            } else {
                if (sWH_Bmp > sWH_View) {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWH_View * sWH_Bmp,
                            1 / sWH_View * sWH_Bmp, 3, 7);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH_Bmp / sWH_View,
                            sWH_Bmp / sWH_View, 3, 7);
                }
            }
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

            float[] result = new float[16];
            Matrix.multiplyMV(result, 0, mMVPMatrix, 0, enlargeTextureParam, 0);
        }
    }


    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static class ColorRender implements GLSurfaceView.Renderer {

        public List<RenderObject> renderObjectList = new ArrayList<>();

        public void disableAll() {
            CollectUtil.execute(renderObjectList, new CollectUtil.Executor<RenderObject>() {
                @Override
                public void execute(RenderObject renderObject) {
                    renderObject.isEffective = false;
                }
            });
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            CollectUtil.execute(renderObjectList, new CollectUtil.Executor<RenderObject>() {
                @Override
                public void execute(RenderObject renderObject) {
                    renderObject.createProgram();
                }
            });
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            CollectUtil.execute(renderObjectList, new CollectUtil.Executor<RenderObject>() {
                @Override
                public void execute(RenderObject renderObject) {
                    renderObject.onSurfaceChanged(gl, width, height);
                }
            });
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            CollectUtil.execute(renderObjectList, new CollectUtil.Executor<RenderObject>() {
                @Override
                public void execute(RenderObject renderObject) {
                    renderObject.onDrawFrame(gl);
                }
            });
        }

    }
}
