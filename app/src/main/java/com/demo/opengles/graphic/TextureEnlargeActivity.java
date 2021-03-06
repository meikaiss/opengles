package com.demo.opengles.graphic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.CollectUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureEnlargeActivity extends BaseActivity {

    //放大镜效果-顶点着色器
    private final String vertexShaderCode =
            "uniform vec4 enlargeParam;\n" +
                    "\n" +
                    "attribute vec4 vPosition;\n" +
                    "attribute vec2 vCoordinate;\n" +
                    "\n" +
                    "varying vec2 aCoordinate;\n" +
                    "varying vec4 gPosition;\n" +
                    "varying vec4 vEnlargeParam;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    gl_Position=vPosition;\n" +
                    "    aCoordinate=vCoordinate;\n" +
                    "    gPosition=vPosition;\n" +
                    "    vEnlargeParam=enlargeParam;\n" +
                    "}";

    //放大镜效果-片元着色器
    //同样一块物理区域，原本纹理坐标是4-6的范围，现在是2-3的范围，自然会放大
    private final String fragmentShaderCode_1 =
            "precision mediump float;\n" +
                    "uniform sampler2D vTexture;\n" +
                    "\n" +
                    "varying vec2 aCoordinate;\n" +
                    "varying vec4 gPosition;\n" +
                    "varying vec4 vEnlargeParam;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    vec4 nColor=texture2D(vTexture,aCoordinate);\n" +
                    "    float dis=distance(vec2(gPosition.x,gPosition.y)," +
                    "                       vec2(vEnlargeParam.r,vEnlargeParam.g));\n" +
                    "    if(dis<vEnlargeParam.b){\n" +
                    "        float textureCenterX=(vEnlargeParam.r+1.0)*0.5;" +
                    "        float textureCenterY=-(vEnlargeParam.g-1.0)*0.5;" +
                    "        float deltaX=(textureCenterX-aCoordinate.x)*0.5;" +
                    "        float deltaY=(textureCenterY-aCoordinate.y)*0.5;" +
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
        setContentView(R.layout.activity_texture_enlarge);

        surfaceContainer = findViewById(R.id.surface_container);
        glSurfaceView = new GLSurfaceView(this);
        surfaceContainer.addView(glSurfaceView);

        textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo_square);
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

                int eventX = (int) event.getX();
                int eventY = (int) event.getY();

                eventX = Math.max(eventX, 0);
                eventX = Math.min(eventX, glSurfaceView.getWidth());
                eventY = Math.max(eventY, 0);
                eventY = Math.min(eventY, glSurfaceView.getHeight());

                float x = (float) eventX / glSurfaceView.getWidth() * 2 - 1.0f; //[-1,1]
                float y = (float) eventY / glSurfaceView.getHeight() * 2 - 1.0f; //[-1,1]
                // 屏幕左上角的坐标是[-1,-1]

                //sdk-View坐标系的y正方向向下，opengl世界坐标系的y正方向向上，因此转换时需要置负
                y = -y; //y->[1,-1]  屏幕左上角的坐标是[-1,1]

                Log.e("mk", "x=" + x + ",y=" + y);

                render.renderObjectList.get(0).enlargeParam[0] = x;
                render.renderObjectList.get(0).enlargeParam[1] = y;

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
        private int glEnlargeParam;

        private int textureId;

        private static int COORDS_PER_VERTEX = 2;
        //顶点个数
        private int vertexCount;
        //顶点之间的偏移量，即每一个顶点所占用的字节大小，每个顶点的坐标有3个float数字，所以为3*4
        private int vertexStride; // 每个float四个字节

        private int vertexShaderIns;
        private int fragmentShaderIns;

        //放大的参数，第1、2个参数表示放大的中心点在世界坐标系中的坐标；第3个参数表示放大的半径；第4个参数无意义，仅用于与透视矩阵4x4相乘
        private float[] enlargeParam = new float[]{0.0f, 0.0f, 0.5f, 0.0f};

        private float[] mViewMatrix = new float[16];
        private float[] mProjectMatrix = new float[16];

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

            //获取句柄，用于将内存中的顶点坐标传递给GPU
            glVPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
            //获取句柄，用于将内存中的纹理坐标传递给GPU
            glVCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
            glVTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
            glEnlargeParam = GLES20.glGetUniformLocation(mProgram, "enlargeParam");

            textureId = createTexture();
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

            GLES20.glUniform4fv(glEnlargeParam, 1, enlargeParam, 0);

            GLES20.glEnableVertexAttribArray(glVPosition);
            GLES20.glVertexAttribPointer(glVPosition, 2, GLES20.GL_FLOAT, false, vertexStride
                    , vertexBuffer);

            GLES20.glEnableVertexAttribArray(glVCoordinate);
            GLES20.glVertexAttribPointer(glVCoordinate, 2, GLES20.GL_FLOAT, false,
                    vertexStride, textureCoordBuffer);

            //将显卡中的第0号纹理单元 赋值给 纹理句柄
            GLES20.glUniform1i(glVTexture, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
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
