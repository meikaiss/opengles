package com.demo.opengles.graphic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.OnSeekBarChangeListenerImpl;
import com.demo.opengles.util.CollectUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TransformActivity extends BaseActivity {

    private final String vertexShaderCode =
            "uniform mat4 vMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aColor;" +
                    "varying  vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*aPosition;" +
                    "  vColor=aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    //每个顶点的坐标，z轴坐标需要启用openGL深度功能
    final float vertexCoords[] = {
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

    private Bitmap textureBmp;

    private FrameLayout surfaceContainer;
    private GLSurfaceView glSurfaceView;
    private TransformRender render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform);

        surfaceContainer = findViewById(R.id.surface_container);
        glSurfaceView = new GLSurfaceView(this);
        surfaceContainer.addView(glSurfaceView);

        textureBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo);
        glSurfaceView.setEGLContextClientVersion(2);

        render = new TransformRender();
        {
            RenderObject object1 = new RenderObject();
            object1.init(vertexShaderCode, fragmentShaderCode, vertexCoords, index, color,
                    textureBmp, true);

            render.renderObjectList.add(object1);
        }

        glSurfaceView.setRenderer(render);

        //必须在setRenderer之后才能调用
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        initView();
    }

    private CheckBox ckbX;
    private CheckBox ckbY;
    private CheckBox ckbZ;
    private Button btnReset;

    private void initView() {
        ckbX = findViewById(R.id.ckb_x);
        ckbY = findViewById(R.id.ckb_y);
        ckbZ = findViewById(R.id.ckb_z);
        btnReset = findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ckbX.setChecked(true);
                ckbY.setChecked(false);
                ckbZ.setChecked(false);
                RenderObject object1 = render.renderObjectList.get(0);
                object1.mMatrixTranslate = null;
                object1.mMatrixRotate = null;
                object1.mMatrixScale = null;
                glSurfaceView.requestRender();
            }
        });
        ((SeekBar) findViewById(R.id.seek_bar_1)).setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= 50;
                float translate = progress / 50f;

                RenderObject object1 = render.renderObjectList.get(0);
                object1.mMatrixTranslate = new float[16];
                Matrix.setIdentityM(object1.mMatrixTranslate, 0);
                Matrix.translateM(object1.mMatrixTranslate, 0,
                        ckbX.isChecked() ? translate : 0,
                        ckbY.isChecked() ? translate : 0,
                        ckbZ.isChecked() ? translate : 0);

                glSurfaceView.requestRender();
            }
        });

        ((SeekBar) findViewById(R.id.seek_bar_2)).setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= 50;
                float rotateAngle = progress / 50f * 360;

                RenderObject object1 = render.renderObjectList.get(0);
                object1.mMatrixRotate = new float[16];
                Matrix.setIdentityM(object1.mMatrixRotate, 0);
                Matrix.rotateM(object1.mMatrixRotate, 0, rotateAngle,
                        ckbX.isChecked() ? 1 : 0,
                        ckbY.isChecked() ? 1 : 0,
                        ckbZ.isChecked() ? 1 : 0);

                glSurfaceView.requestRender();
            }
        });

        ((SeekBar) findViewById(R.id.seek_bar_3)).setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= 50;
                float scale = progress / 50f;

                RenderObject object1 = render.renderObjectList.get(0);
                object1.mMatrixScale = new float[16];
                Matrix.setIdentityM(object1.mMatrixScale, 0);
                Matrix.scaleM(object1.mMatrixScale, 0,
                        ckbX.isChecked() ? scale : 1,
                        ckbY.isChecked() ? scale : 1,
                        ckbZ.isChecked() ? scale : 1);

                glSurfaceView.requestRender();
            }
        });
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static class RenderObject {

        private String vertexShaderCode;
        private String fragmentShaderCode;

        private float[] vertexCoords;
        private short[] index;
        private float[] color;

        private FloatBuffer vertexBuffer;
        private FloatBuffer colorBuffer;
        private ShortBuffer indexBuffer;

        private int mProgram;

        private int mMatrixHandler;
        private int mPositionHandle;
        private int mColorHandle;

        private int textureId;

        private int vertexShaderIns;
        private int fragmentShaderIns;

        private float[] mViewMatrix = new float[16];
        private float[] mProjectMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];

        private float[] mMatrixTranslate = null;
        private float[] mMatrixRotate = null;
        private float[] mMatrixScale = null;

        public Bitmap textureBmp;

        public boolean isEffective;

        private int COORDS_PER_VERTEX = 3;  //每个顶点有3个数字来表示它的坐标
        private int COORDS_PER_COLOR = 4;  //每个颜色值有4个数字来表示它的内容
        private int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点的坐标有3个数值，数值都是float类型，每个float
        private int colorStride = COORDS_PER_COLOR * 4; // 每个float四个字节

        public void init(String vertexShaderCode, String fragmentShaderCode,
                         float[] vertexCoords, short[] index, float[] color,
                         Bitmap textureBmp, boolean isEffective) {
            this.vertexShaderCode = vertexShaderCode;
            this.fragmentShaderCode = fragmentShaderCode;
            this.vertexCoords = vertexCoords;
            this.index = index;
            this.color = color;
            this.textureBmp = textureBmp;
            this.isEffective = isEffective;
        }

        public void createProgram() {
            //rgb=0.4表示背景为灰色
            GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
            //启用2d纹理功能，包含2d采样; 开启深度测试
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            //开启深度测试
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            //将内存中的顶点坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
            ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertexCoords);
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

            //获取变换矩阵vMatrix成员句柄
            mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
            //获取顶点着色器的vPosition成员句柄
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            //获取片元着色器的vColor成员的句柄
            mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        }

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

        public void onDrawFrame(GL10 gl) {
            if (!isEffective) {
                return;
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);

            float[] matrixResult = new float[16];
            for (int i = 0; i < matrixResult.length; i++) {
                matrixResult[i] = mMVPMatrix[i];
            }
            if (mMatrixTranslate != null) {
                Matrix.multiplyMM(matrixResult, 0, matrixResult, 0, mMatrixTranslate, 0);
            }
            if (mMatrixRotate != null) {
                Matrix.multiplyMM(matrixResult, 0, matrixResult, 0, mMatrixRotate, 0);
            }
            if (mMatrixScale != null) {
                Matrix.multiplyMM(matrixResult, 0, matrixResult, 0, mMatrixScale, 0);
            }

            //指定vMatrix的值
            GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, matrixResult, 0);

            //启用三角形顶点的句柄
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            //准备三角形的坐标数据
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

            GLES20.glEnableVertexAttribArray(mColorHandle);
            GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
                    GLES20.GL_FLOAT, false, colorStride, colorBuffer);

            //用索引法来绘制三角形，最张这些三角形就会组合成一个正方体
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT
                    , indexBuffer);
            //禁止顶点数组的句柄
            GLES20.glDisableVertexAttribArray(mPositionHandle);
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
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static class TransformRender implements GLSurfaceView.Renderer {

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
