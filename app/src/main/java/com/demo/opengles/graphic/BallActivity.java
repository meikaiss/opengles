package com.demo.opengles.graphic;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.ToastUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BallActivity extends BaseActivity {

    private FloatBuffer vertexBuffer;  //球面顶点buffer

    /**
     * 此shader的算法让z越高的地方颜色越白，z越小的地方颜色越黑；
     * 当眼睛位于x轴向原点方向观察时，屏幕显示即为左右为白色，中间为黑色
     */
    private final String vertexShaderCode =
            "uniform mat4 vMatrix;\n" +
                    "varying vec4 vColor;\n" +
                    "attribute vec4 vPosition;\n" +
                    "void main(){\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    float color;\n" +
                    "    if(vPosition.z>0.0){\n" +
                    "        color=vPosition.z;\n" +
                    "    }else{\n" +
                    "        color=-vPosition.z;\n" +
                    "    }\n" +
                    "    vColor=vec4(color,color,color,1.0);\n" +
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

    private float step = 1f;


    private float[] shapePos;

    private float radius = 2.0f;

    //设置圆锥中心顶点的颜色值
    float centerColor[] = {0.0f, 1.0f, 0.0f, 1.0f};

    //球面顶点坐标数组，每个顶点都是三维坐标
    private float[] createBallPos() {
        //球以(0,0,0)为中心，以R为半径，则球上任意一点的坐标为
        // ( R * cos(a) * sin(b),y0 = R * sin(a),R * cos(a) * cos(b))
        // 其中，a为圆心到点的线段与xz平面的夹角，b为圆心到点的线段在xz平面的投影与z轴的夹角
        ArrayList<Float> data = new ArrayList<>();
        float r1, r2;
        float h1, h2;
        float sin, cos;
        for (float i = -90; i < 90 + step; i += step) {
            r1 = (float) Math.cos(i * Math.PI / 180.0);
            r2 = (float) Math.cos((i + step) * Math.PI / 180.0);
            h1 = (float) Math.sin(i * Math.PI / 180.0);
            h2 = (float) Math.sin((i + step) * Math.PI / 180.0);
            // 固定纬度, 360 度旋转遍历一条纬线
            float step2 = step * 2;
            for (float j = 0.0f; j < 360.0f + step; j += step2) {
                cos = (float) Math.cos(j * Math.PI / 180.0);
                sin = -(float) Math.sin(j * Math.PI / 180.0);

                data.add(r2 * cos);
                data.add(h2);
                data.add(r2 * sin);
                data.add(r1 * cos);
                data.add(h1);
                data.add(r1 * sin);
            }
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(this);
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        frameLayout.addView(glSurfaceView);
        {
            Button button = new Button(this);
            button.setText("+");
            frameLayout.addView(button,
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (step == 0.5f) {
                        step = 1.0f;
                    } else {
                        step++;
                    }

                    ToastUtil.show("step=" + step);
                    glSurfaceView.requestRender();
                }
            });
        }
        {
            Button button = new Button(this);
            button.setText("-");
            FrameLayout.LayoutParams lp =
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.END;
            frameLayout.addView(button, lp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    step--;
                    if (step <= 0) {
                        step = 0.5f;
                    }
                    ToastUtil.show("step=" + step);
                    glSurfaceView.requestRender();
                }
            });
        }


        setContentView(frameLayout);

        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                //开启深度测试
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                //将背景设置为灰色
                GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

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
                Matrix.setLookAtM(mViewMatrix, 0, 7.0f, 0.0f, 0.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                //计算变换矩阵
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //用onCreate中通过GLES20.glClearColor指定的颜色来刷新缓冲区
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                //将程序加入到OpenGLES2.0环境
                GLES20.glUseProgram(mProgram);

                shapePos = createBallPos();
                ByteBuffer bb = ByteBuffer.allocateDirect(shapePos.length * 4);
                bb.order(ByteOrder.nativeOrder());

                vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.put(shapePos);
                vertexBuffer.position(0);

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
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer);

                //获取片元着色器的vColor成员的句柄
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
                //设置绘制三角形的颜色
                GLES20.glUniform4fv(mColorHandle, 1, centerColor, 0);

                //绘制三角形
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePos.length / 3);

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
