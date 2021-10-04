package com.demo.opengles.graphic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FrameBufferActivity extends BaseActivity {

    private static final String TAG = "FrameBufferActivity";

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_buffer);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //设置调用清除方法后，背景显示的颜色，通过用于设置默认颜色
                GLES20.glClearColor(0.4f, 0.0f, 0.0f, 1.0f);

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //执行此方法后，显示区域会呈现为GLES20.glClearColor设定的颜色
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                draw();
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final String vertexShaderCode =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aCoord;\n" +
                    "varying vec2 vCoord;\n" +
                    "void main(){\n" +
                    "    gl_Position=aPosition;\n" +
                    "    vCoord=aCoord;\n" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D uTexture;\n" +
                    "varying vec2 vCoord;\n" +
                    "void main(){\n" +
                    "    gl_FragColor=texture2D(uTexture,vCoord);\n" +
                    "}";

    private int framebufferId;
    private int FBOTextureId;
    private int LoadedTextureId;
    private int program;
    private FloatBuffer vertexFBOTextureCoordBuffer;

    //顶点坐标+FBO纹理坐标+正常纹理坐标，各占两个float，即在{x1y1x2y2x3y3}中顶点坐标=x1y1，FBO纹理坐标=x2y2，正常纹理坐标=x3y3
    private final float[] vertexFBOTextureCoords = {
            -1.0f, 1.0f, 0f, 1f, 0f, 0f,
            -1.0f, -1.0f, 0f, 0f, 0f, 1.0f,
            1.0f, 1.0f, 1f, 1f, 1.0f, 0f,
            1.0f, -1.0f, 1f, 0f, 1.0f, 1.0f
    };

    private void draw() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexFBOTextureCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexFBOTextureCoordBuffer = bb.asFloatBuffer();
        vertexFBOTextureCoordBuffer.put(vertexFBOTextureCoords);
        vertexFBOTextureCoordBuffer.position(0);

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


        //生成一个离屏缓存对象，把它们的句柄依次放于int数组中
        int[] frameBufferArr = new int[1];
        GLES20.glGenFramebuffers(1, frameBufferArr, 0);
        framebufferId = frameBufferArr[0];
        //生成两个纹理，一个用于装载已有图案，一个用于挂载给FBO
        int[] textureArr = new int[2];
        GLES20.glGenTextures(2, textureArr, 0);
        LoadedTextureId = textureArr[0];
        FBOTextureId = textureArr[1];


        //给渲染引擎绑定一个纹理，纹理类型由参数1决定，纹理存储地址由参数2决定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LoadedTextureId);
        //绑定纹理后，后续代码对文理参数的操作，均指代上一次绑定的纹理。设置采样，拉伸方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_MIRRORED_REPEAT);
        Bitmap bitmap_markPolo = BitmapFactory.decodeResource(getResources(),
                R.mipmap.texture_image_markpolo);
        //将马可波罗的图片设置给此纹理，作为已装载的纹理图案数据
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap_markPolo, 0);
        //解除纹理绑定，后续不再操作已装载进显存的马可波罗纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        //切换渲染引擎绑定的纹理，改为 FBO纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, FBOTextureId);
        //设置FBO纹理采样，拉伸方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_MIRRORED_REPEAT);
        //设置FBO纹理的图像数据格式
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, glSurfaceView.getWidth(),
                glSurfaceView.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);


        //将渲染引擎当前绘制的对象，切换到帧缓冲。默认的绘制对象即为屏幕硬件
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        //将准备好的FBO纹理设置给帧缓冲，帧缓冲从肉眼感知上可以理解为看不见的屏幕
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, FBOTextureId, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "glFramebufferTexture2D error");
        }
        //解除FBO纹理绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        int frameBufferVertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int frameBufferFragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, frameBufferVertexShader);
        GLES20.glAttachShader(program, frameBufferFragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        int vertexHandle = GLES20.glGetAttribLocation(program, "aPosition");
        int textureCoordHandle = GLES20.glGetAttribLocation(program, "aCoord");
        int textureHandle = GLES20.glGetUniformLocation(program, "uTexture");

        /**
         * 顶点坐标和FBO纹理坐标都旋转到一个FloatBuffer缓冲了，前两位表示顶点坐标，后两个表示FBO纹理坐标
         * 因此顶点坐标的偏移为0，FBO纹理坐标的偏移为2，正常纹理坐标偏移为2+2
         * size表示每个坐标所占用的float个数，这里我们都只使用了2维数据，因此都是2
         *步长为(2+2+2)*4，其中2+2+2表示前两个是顶点坐标，后两个是FBO纹理坐标，*4表示一个float占用4个字节
         */
        int floatStride = 2 + 2 + 2; //一组坐标的float个数
        int stride = floatStride * 4; //一组坐标的字节个数
        vertexFBOTextureCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(vertexHandle, 2, GLES20.GL_FLOAT, false, stride,
                vertexFBOTextureCoordBuffer);
        vertexFBOTextureCoordBuffer.position(2);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, stride,
                vertexFBOTextureCoordBuffer);
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        //启用纹理硬件组中的第0号纹理引脚
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //给启用的纹理引脚绑定一个纹理，纹理类型由参数1决定，纹理的存储地址由参数2决定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LoadedTextureId);
        //将0号引脚上的纹理传递给着色器中的纹理句柄
        GLES20.glUniform1i(textureHandle, 0);
        //绘制图形时所使用的顶点的个数
        int vertexCount = vertexFBOTextureCoordBuffer.capacity() / floatStride;
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        //向FBO绘制完数据后，将FBO从渲染引擎上解绑，解绑后的绘制即会显示到屏幕上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        /**
         * 向FBO渲染完成后，为了证明FBO的功能，故意修改纹理LoadedTextureId的图像，从之前的马可波罗改为亚瑟
         */
        //给渲染引擎绑定当前启用的纹理，纹理类型由参数1决定，纹理地址由参数2决定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LoadedTextureId);
        //设置采样，拉伸方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_MIRRORED_REPEAT);
        Bitmap bitmap_arthur = BitmapFactory.decodeResource(getResources(),
                R.mipmap.texture_image_arthur);
        //给渲染引擎当前启用的纹理填充图像数据
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap_arthur, 0);
        //解除纹理绑定，告诉渲染引擎，后续不再需要操作纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        //启用纹理硬件组中的第1号纹理引脚
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        /**
         * 给启用的纹理引脚绑定一个纹理，纹理类型由参数1决定，纹理的存储地址由参数2决定
         * 同时由于FBOTextureId已经挂载到FBO，所以之前绘制的马可波罗图像会映射到FBOTextureId纹理。
         * 那么后续读取FBOTextureId的纹理图像数据时，即会读出马可波罗图像
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, FBOTextureId);
        //将1号引脚上的纹理传递给着色器中的纹理句柄
        GLES20.glUniform1i(textureHandle, 1);
        vertexFBOTextureCoordBuffer.position(4);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, stride,
                vertexFBOTextureCoordBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        GLES20.glDeleteTextures(2, textureArr, 0);
        GLES20.glDeleteFramebuffers(1, frameBufferArr, 0);
    }
}
