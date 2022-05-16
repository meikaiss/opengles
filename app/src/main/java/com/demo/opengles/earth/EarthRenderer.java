package com.demo.opengles.earth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.demo.opengles.R;
import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EarthRenderer implements GLSurfaceView.Renderer {
    private static final int BYTES_PER_FLOAT = 4;

    private Context context;
    private int width;
    private int height;

    //顶点位置缓存
    private FloatBuffer vertexBuffer;
    //纹理顶点位置缓存
    private FloatBuffer mTexVertexBuffer;
    private int vertexCount;
    //渲染程序
    private int mProgram;

    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //模型矩阵
    private final float[] mModelMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    //返回属性变量的位置
    //变换矩阵
    private int uMatrixLocation;
    //位置
    private int aPositionLocation;
    private int mHCoordinate;

    private Bitmap mBitmap;
    private int textureId;

    public float eyeX = 2f;
    public float eyeY = 2f;
    public float eyeZ = 2f;

    public EarthRenderer(Context context) {
        this.context = context;

        createVertexBuffer();
    }

    private void createVertexBuffer() {
        ArrayList<Float> alVertex = new ArrayList<>();
        ArrayList<Float> textureVertex = new ArrayList<>();

        float radius = 1.0f; // 球的半径
        double angleSpan = Math.PI / 90f; // 将球进行单位切分的角度

        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan) {

            for (double hAngle = 0; hAngle < 2 * Math.PI; hAngle = hAngle + angleSpan) {
                //获取一个四边形的四个顶点
                float x0 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius * Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius * Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                float s0 = (float) (-hAngle / Math.PI / 2);
                float s1 = (float) (-(hAngle + angleSpan) / Math.PI / 2);

                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                //将四个点拆分为两个三角形
                alVertex.add(x1);
                alVertex.add(y1);
                alVertex.add(z1);
                alVertex.add(x0);
                alVertex.add(y0);
                alVertex.add(z0);
                alVertex.add(x3);
                alVertex.add(y3);
                alVertex.add(z3);

                textureVertex.add(s1);// x1 y1对应纹理坐标
                textureVertex.add(t0);
                textureVertex.add(s0);// x0 y0对应纹理坐标
                textureVertex.add(t0);
                textureVertex.add(s0);// x3 y3对应纹理坐标
                textureVertex.add(t1);

                alVertex.add(x1);
                alVertex.add(y1);
                alVertex.add(z1);
                alVertex.add(x3);
                alVertex.add(y3);
                alVertex.add(z3);
                alVertex.add(x2);
                alVertex.add(y2);
                alVertex.add(z2);

                textureVertex.add(s1);// x1 y1对应纹理坐标
                textureVertex.add(t0);
                textureVertex.add(s0);// x3 y3对应纹理坐标
                textureVertex.add(t1);
                textureVertex.add(s1);// x2 y3对应纹理坐标
                textureVertex.add(t1);
            }
        }

        vertexBuffer = convertToFloatBuffer(alVertex);
        mTexVertexBuffer = convertToFloatBuffer(textureVertex);

        vertexCount = alVertex.size() / 3;
    }

    //动态数组转FloatBuffer
    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data) {
        float[] d = new float[data.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = data.get(i);
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(data.size() * 4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret = buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        String vertexShaderStr = OpenGLESUtil.getShaderCode(context, "shader/earth/vertex.glsl");
        String fragmentShaderStr = OpenGLESUtil.getShaderCode(context, "shader/earth/fragment.glsl");

        mProgram = OpenGLESUtil.createProgram(vertexShaderStr, fragmentShaderStr);
        GLES30.glUseProgram(mProgram);

        uMatrixLocation = GLES30.glGetUniformLocation(mProgram, "u_Matrix");
        aPositionLocation = GLES30.glGetAttribLocation(mProgram, "vPosition");
        mHCoordinate = GLES30.glGetAttribLocation(mProgram, "aCoordinate");

        //加载纹理
        textureId = loadTexture(context, R.mipmap.earth);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘制窗口
        GLES30.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float ratio = (float) width / height;
        //透视投影矩阵/视锥
        Matrix.perspectiveM(mProjectMatrix, 0, 60, ratio, 1f, 300f);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, 0.0f, 0.0f, 0f, 0f, 1f, 0f);
        //模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.rotateM(mModelMatrix, 0, 40, 1, 0, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);

        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////

        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);


        GLES30.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        //将变换矩阵传入顶点渲染器
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        GLES30.glVertexAttribPointer(aPositionLocation, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(aPositionLocation);

        GLES30.glVertexAttribPointer(mHCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTexVertexBuffer);
        GLES30.glEnableVertexAttribArray(mHCoordinate);

        //绘制球
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionLocation);
        GLES30.glDisableVertexAttribArray(mHCoordinate);
    }

    private int loadTexture(Context context, int resourceId) {
        final int[] textureIds = new int[1];
        //创建一个纹理对象
        GLES30.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //这里需要加载原图未经缩放的数据
        options.inScaled = false;
        mBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (mBitmap == null) {
            GLES30.glDeleteTextures(1, textureIds, 0);
            return 0;
        }
        //绑定纹理到OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        //设置默认的纹理过滤参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        //生成MIP贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        //数据如果已经被加载进OpenGL,则可以回收该bitmap
        mBitmap.recycle();

        //取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }
}

