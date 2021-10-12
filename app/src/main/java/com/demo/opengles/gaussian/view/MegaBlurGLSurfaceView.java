package com.demo.opengles.gaussian.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.Surface;
import android.view.ViewGroup;


import com.demo.opengles.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MegaBlurGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private int mMaxBlurCount = 6;
    private static final int BLUR_RADIUS = 30;
    private int mBlurCount = 0;
    private boolean mCouldDraw = false;
    private boolean mCouldScreenShot = true;
    private boolean mUpdateBackground = false;
    private OnGLSurfaceActionListener mOnGLSurfaceActionListener;

    private static final float[] VERTEX_DATA = new float[]{
            // Order of coordinates: X, Y, S, T
            // Triangle Fan
            0.0f, 0.0f, 0.5f, 0.5f,
            -1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f, 1.0f
    };

    private FloatBuffer mFloatBuffer = ByteBuffer
            .allocateDirect(VERTEX_DATA.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(VERTEX_DATA);

    final String vertexShader = "uniform mat4 u_Matrix;\n"
            + "\n"
            + "attribute vec4 a_Position;  \n"
            + "attribute vec2 a_TextureCoordinates;\n"
            + "\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "\n"
            + "void main()                    \n"
            + "{                            \n"
            + "    v_TextureCoordinates = a_TextureCoordinates;\n"
            + "    gl_Position = u_Matrix * a_Position;    \n"
            + "}";

    final String horizontalFragmentShaderForOES = "#extension GL_OES_EGL_image_external :"
            + " require\n"
            + "precision mediump float;\n"
            + "\n"
            + "\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "uniform samplerExternalOES mainTexture;\n"
            + "uniform int blurRadius;\n"
            + "\n"
            + "\n"
            + "uniform float textureWidth;\n"
            + "uniform float textureHeight;\n"
            + "uniform float scale;\n"
            + "\n"
            + "mediump float getGaussWeight(mediump float currentPos, mediump float "
            + "sigma)\n"
            + "{\n"
            + "    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * "
            + "sigma));\n"
            + "}\n"
            + "\n"
            + "void main() {\n"
            + "    int diameter = 2 * blurRadius + 1;\n"
            + "    vec4 sampleTex;\n"
            + "    vec4 sampleTexCurrent;\n"
            + "    vec4 col;\n"
            + "    float weightSum = 0.0;\n"
            + "    vec2 flippedYUV = v_TextureCoordinates;\n"
            + "    flippedYUV.y = 1.0 - flippedYUV.y;\n"
            + "\n"
            + "    int step = int(float( blurRadius/15)*scale)+1;\n"
            + "\n"
            + "    sampleTexCurrent = vec4(texture2D(mainTexture, flippedYUV.st));\n"
            + "    for(int i = 0; i < diameter; i+=step) {\n"
            + "       vec2 offset = vec2(float(i - blurRadius) * textureWidth,  float(i -"
            + " blurRadius) * textureHeight);\n"
            + "       if(flippedYUV.t + offset.t > 1.0 || flippedYUV.t + offset.t < 0.0) {\n"
            + "           offset.t = 0.0;\n"
            + "       }\n"
            + "       if(flippedYUV.s + offset.s > 1.0 || flippedYUV.s + offset.s < 0.0) {\n"
            + "           offset.s = 0.0;\n"
            + "       }\n"
            + "       sampleTex = vec4(texture2D(mainTexture, flippedYUV.st+offset));\n"
            + "       float index = float(i);\n"
            + "       float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,"
            + "  (float(diameter - 1)/2.0 + 1.0) / 2.0);\n"
            + "       if (sampleTex.a == 1.0) {\n"
            + "       col += sampleTex * gaussWeight;\n"
            + "       } else {\n"
            + "       col += sampleTexCurrent * gaussWeight;\n"
            + "       }\n"
            + "       weightSum += gaussWeight;\n"
            + "    }\n"
            + "\n"
            + "    gl_FragColor =  vec4(col.rgb / weightSum, sampleTexCurrent.a);\n"
            + "}";

    final String horizontalFragmentShader = "precision mediump float;\n"
            + "\n"
            + "\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "uniform sampler2D mainTexture;\n"
            + "uniform int blurRadius;\n"
            + "\n"
            + "\n"
            + "uniform float textureWidth;\n"
            + "uniform float textureHeight;\n"
            + "uniform float scale;\n"
            + "\n"
            + "mediump float getGaussWeight(mediump float currentPos, mediump float "
            + "sigma)\n"
            + "{\n"
            + "    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * "
            + "sigma));\n"
            + "}\n"
            + "\n"
            + "void main() {\n"
            + "    int diameter = 2 * blurRadius + 1;\n"
            + "    vec4 sampleTex;\n"
            + "    vec4 sampleTexCurrent;\n"
            + "    vec4 col;\n"
            + "    float weightSum = 0.0;\n"
            + "\n"
            + "    int step = int(float( blurRadius/15)*scale)+1;\n"
            + "\n"
            + "    sampleTexCurrent = vec4(texture2D(mainTexture, v_TextureCoordinates.st));\n"
            + "    for(int i = 0; i < diameter; i+=step) {\n"
            + "       vec2 offset = vec2(float(i - blurRadius) * textureWidth,  float(i -"
            + " blurRadius) * textureHeight);\n"
            + "       if(v_TextureCoordinates.t + offset.t > 1.0 || v_TextureCoordinates.t + "
            + "offset.t < 0.0) {\n"
            + "           offset.t = 0.0;\n"
            + "       }\n"
            + "       if(v_TextureCoordinates.s + offset.s > 1.0 || v_TextureCoordinates.s + "
            + "offset.s < 0.0) {\n"
            + "           offset.s = 0.0;\n"
            + "       }\n"
            + "       sampleTex = vec4(texture2D(mainTexture, v_TextureCoordinates.st+offset));\n"
            + "       float index = float(i);\n"
            + "       float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,"
            + "  (float(diameter - 1)/2.0 + 1.0) / 2.0);\n"
            + "       if (sampleTex.a == 1.0) {\n"
            + "       col += sampleTex * gaussWeight;\n"
            + "       } else {\n"
            + "       col += sampleTexCurrent * gaussWeight;\n"
            + "       }\n"
            + "       weightSum += gaussWeight;\n"
            + "    }\n"
            + "\n"
            + "    gl_FragColor =  vec4(col.rgb / weightSum, sampleTexCurrent.a);\n"
            + "}";

    final String verticalFragmentShader = "precision mediump float;\n"
            + "\n"
            + "\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "uniform sampler2D mainTexture;\n"
            + "uniform int blurRadius;\n"
            + "\n"
            + "\n"
            + "uniform float textureWidth;\n"
            + "uniform float textureHeight;\n"
            + "uniform float scale;\n"
            + "\n"
            + "mediump float getGaussWeight(mediump float currentPos, mediump float "
            + "sigma)\n"
            + "{\n"
            + "    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * "
            + "sigma));\n"
            + "}\n"
            + "\n"
            + "void main() {\n"
            + "    int diameter = 2 * blurRadius + 1;\n"
            + "    vec4 sampleTex;\n"
            + "    vec4 sampleTexCurrent;\n"
            + "    vec4 col;\n"
            + "    float weightSum = 0.0;\n"
            + "\n"
            + "    int step = int(float( blurRadius/15)*scale)+1;\n"
            + "\n"
            + "    sampleTexCurrent = vec4(texture2D(mainTexture, v_TextureCoordinates.st));\n"
            + "    for(int i = 0; i < diameter; i+=step) {\n"
            + "        vec2 offset = vec2(float(i - blurRadius) * textureWidth,  float(i "
            + "- blurRadius) * textureHeight);\n"
            + "        if(v_TextureCoordinates.s + offset.s > 1.0 || v_TextureCoordinates.s + "
            + "offset.s < 0.0) {\n"
            + "           offset.s = 0.0;\n"
            + "        }\n"
            + "        if(v_TextureCoordinates.t + offset.t > 1.0 || v_TextureCoordinates.t + "
            + "offset.t < 0.0) {\n"
            + "           offset.t = 0.0;\n"
            + "        }\n"
            + "        sampleTex = vec4(texture2D(mainTexture, v_TextureCoordinates"
            + ".st+offset));\n"
            + "        float index = float(i);\n"
            + "        float gaussWeight = getGaussWeight(index - float(diameter - 1)/2"
            + ".0,  (float(diameter - 1)/2.0 + 1.0) / 2.0);\n"
            + "       if (sampleTex.a == 1.0) {\n"
            + "       col += sampleTex * gaussWeight;\n"
            + "       } else {\n"
            + "       col += sampleTexCurrent * gaussWeight;\n"
            + "       }\n"
            + "        weightSum += gaussWeight;\n"
            + "    }\n"
            + "\n"
            //+ "    gl_FragColor = col / weightSum;\n"
            + "    gl_FragColor =  vec4(col.rgb / weightSum, sampleTexCurrent.a);\n"
            + "}";

    String finalVertex = "uniform mat4 u_Matrix;\n"
            + "\n"
            + "attribute vec4 a_Position;  \n"
            + "attribute vec2 a_TextureCoordinates;\n"
            + "\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "\n"
            + "void main()                    \n"
            + "{                            \n"
            + "    v_TextureCoordinates = a_TextureCoordinates;\n"
            + "    gl_Position = u_Matrix * a_Position;    \n"
            + "}";

    String finalFragment = "precision mediump float;\n"
            + "\n"
            + "uniform sampler2D mainTexture;\n"
            + "varying vec2 v_TextureCoordinates;\n"
            + "\n"
            + "void main()\n"
            + "{\n"
            + "    gl_FragColor = texture2D(mainTexture, v_TextureCoordinates);\n"
            + "}";

    int[] textures;
    int programHori;
    int programHoriForOES;
    int programVert;
    int programFinal;

    int horiFbo;
    int horiBuffer;
    int horiRender;
    int vertFbo;
    int vertBuffer;
    int vertRender;

    float[] pm = new float[16];
    SurfaceTexture surfaceTexture;
    Surface surface;
    Bitmap screenShot;
    int mWidth;
    int mHeight;
    Path mPath;
    private boolean mUseExternalScreenShot = false;
    private boolean mIsRepeatScreenShot = true;

    public MegaBlurGLSurfaceView(Context context) {
        super(context);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(false);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public void updatePath(Path path, boolean updateBackground) {

        if (mPath != path) {
            mPath = path;
            mUpdateBackground = updateBackground;
            mCouldDraw = false;

            if (screenShot == null || screenShot.isRecycled() || mUpdateBackground) {
                mUpdateBackground = false;
                int[] point = new int[2];
                if (getParent() != null && getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).getLocationOnScreen(point);
                } else {
                    getLocationOnScreen(point);
                }

                if (mWidth != 0 && mHeight != 0 && mCouldScreenShot) {
                    screenShot = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo);
                    mCouldScreenShot = false;
                }
            }

            if (surface != null && screenShot != null && !screenShot.isRecycled()) {
                try {
                    Canvas canvas = surface.lockHardwareCanvas();
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

                    canvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
                    if (mPath != null) {
                        canvas.clipPath(mPath);
                    }
                    canvas.drawBitmap(screenShot, 0, 0, null);

                    surface.unlockCanvasAndPost(canvas);
                    surfaceTexture.setOnFrameAvailableListener(
                            surfaceTexture -> {
                                mCouldDraw = true;
                                mBlurCount = 0;
                                requestRender();
                                surfaceTexture.setOnFrameAvailableListener(null);
                            });
                } catch (IllegalStateException e) {
                }
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int shader1 = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        int shader2 = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        int shader3 = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        int shader4 = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        int shader5 = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        int shader6 = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);

        GLES31.glShaderSource(shader1, vertexShader);
        GLES31.glShaderSource(shader2, horizontalFragmentShader);
        GLES31.glShaderSource(shader3, verticalFragmentShader);
        GLES31.glShaderSource(shader4, finalVertex);
        GLES31.glShaderSource(shader5, finalFragment);
        GLES31.glShaderSource(shader6, horizontalFragmentShaderForOES);

        GLES31.glCompileShader(shader1);
        GLES31.glCompileShader(shader2);
        GLES31.glCompileShader(shader3);
        GLES31.glCompileShader(shader4);
        GLES31.glCompileShader(shader5);
        GLES31.glCompileShader(shader6);

        programHori = GLES31.glCreateProgram();
        programVert = GLES31.glCreateProgram();
        programFinal = GLES31.glCreateProgram();
        programHoriForOES = GLES31.glCreateProgram();

        GLES31.glAttachShader(programHori, shader1);
        GLES31.glAttachShader(programHoriForOES, shader1);
        GLES31.glAttachShader(programHori, shader2);
        GLES31.glAttachShader(programHoriForOES, shader6);

        GLES31.glAttachShader(programVert, shader1);
        GLES31.glAttachShader(programVert, shader3);

        GLES31.glAttachShader(programFinal, shader4);
        GLES31.glAttachShader(programFinal, shader5);

        GLES31.glLinkProgram(programHori);
        GLES31.glLinkProgram(programVert);
        GLES31.glLinkProgram(programFinal);
        GLES31.glLinkProgram(programHoriForOES);

        int[] temp = new int[2];
        GLES31.glGenFramebuffers(2, temp, 0);
        horiBuffer = temp[0];
        vertBuffer = temp[1];
        GLES31.glGenTextures(2, temp, 0);
        horiFbo = temp[0];
        vertFbo = temp[1];
        GLES31.glGenRenderbuffers(2, temp, 0);
        horiRender = temp[0];
        vertRender = temp[1];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ZERO);
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES31.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if (width == 0 && height == 0) {
            return;
        }

        mBlurCount = 0;
        mCouldDraw = false;
        mWidth = width;
        mHeight = height;

        int[] point = new int[2];
        if (getParent() != null && getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).getLocationOnScreen(point);
        } else {
            getLocationOnScreen(point);
        }

        if (mCouldScreenShot) {

            if (!mUseExternalScreenShot && mIsRepeatScreenShot) {
                screenShot = screenShot(point, width, height);
            } else {
                if (screenShot == null) {
                    screenShot = screenShot(point, width, height);
                }
            }
            mCouldScreenShot = false;
        }

        GLES31.glViewport(0, 0, width, height);
        float left = -1.0f;
        float right = 1.0f;
        float bottom = 1f;
        float top = -1f;
        float near = -1.0f;
        float far = 1.0f;
        Matrix.setIdentityM(pm, 0);
        Matrix.orthoM(pm, 0, left, right, bottom, top, near, far);

        textures = new int[1];

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);

        GLES31.glGenTextures(1, textures, 0);
        GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES31.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                (float) GLES20.GL_LINEAR);
        GLES31.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                (float) GLES20.GL_LINEAR);
        GLES31.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ZERO);

        surfaceTexture = new SurfaceTexture(textures[0]);
        surfaceTexture.setDefaultBufferSize(width, height);
        surface = new Surface(surfaceTexture);

        Canvas canvas = surface.lockHardwareCanvas();

        canvas.setDrawFilter(
                new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        if (mPath != null) {
            canvas.clipPath(mPath);
        }

        screenShot = BitmapFactory.decodeResource(getResources(), R.mipmap.texture_image_markpolo);
        canvas.drawBitmap(screenShot, 0, 0, null);

        surface.unlockCanvasAndPost(canvas);
        surfaceTexture.setOnFrameAvailableListener(
                surfaceTexture -> {
                    mCouldDraw = true;
                    requestRender();
                    surfaceTexture.setOnFrameAvailableListener(null);
                });

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, horiBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, horiFbo);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, width, height, 0,
                GLES31.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, horiRender);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width,
                height);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, horiFbo, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, horiRender);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, vertBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vertFbo);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, width, height, 0,
                GLES31.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, vertRender);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width,
                height);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, vertFbo, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, vertRender);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private Bitmap screenShot(int[] point, int width, int height) {
        if (mOnGLSurfaceActionListener != null) {
            mOnGLSurfaceActionListener.onScreenShotFinished();
        }
        return screenShot;
    }

    void doHori(int fbo, int height, int r, boolean first) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, horiBuffer);
        GLES31.glBindTexture(first ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES31.GL_TEXTURE_2D, fbo);
        GLES31.glUseProgram(first ? programHoriForOES : programHori);
        GLES31.glUniformMatrix4fv(
                GLES31.glGetUniformLocation(first ? programHoriForOES : programHori, "u_Matrix"), 1,
                false, pm, 0);
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glUniform1i(
                GLES31.glGetUniformLocation(first ? programHoriForOES : programHori, "mainTexture"),
                0);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(first ? programHoriForOES : programHori,
                "textureWidth"), 0f);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(first ? programHoriForOES : programHori,
                "textureHeight"), 1f / ((float) height) / 1.0f);
        GLES20.glUniform1f(
                GLES20.glGetUniformLocation(first ? programHoriForOES : programHori, "scale"),
                1.0f);
        GLES20.glUniform1i(
                GLES20.glGetUniformLocation(first ? programHoriForOES : programHori, "blurRadius"),
                r);
        getUniform(first ? programHoriForOES : programHori);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    void doVert(int fbo, int width, int r) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, vertBuffer);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, fbo);
        GLES31.glUseProgram(programVert);
        GLES31.glUniformMatrix4fv(GLES31.glGetUniformLocation(programVert, "u_Matrix"), 1, false,
                pm, 0);
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glUniform1i(GLES31.glGetUniformLocation(programVert, "mainTexture"), 0);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(programVert, "textureWidth"),
                1f / ((float) width) / 1.0f);
        GLES31.glUniform1f(GLES31.glGetUniformLocation(programVert, "textureHeight"), 0f);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(programVert, "scale"), 1.0f);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programVert, "blurRadius"), r);
        getUniform(programVert);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    void getUniform(int program) {
        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ZERO);
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        int positionAttributeLocation = GLES31.glGetAttribLocation(program, "a_Position");
        int textureCoordinatesAttributeLocation = GLES31.glGetAttribLocation(program,
                "a_TextureCoordinates");

        mFloatBuffer.position(0);
        GLES31.glVertexAttribPointer(positionAttributeLocation, 2, GLES20.GL_FLOAT,
                false, 16, mFloatBuffer);
        GLES31.glEnableVertexAttribArray(positionAttributeLocation);
        mFloatBuffer.position(0);

        mFloatBuffer.position(2);
        GLES31.glVertexAttribPointer(textureCoordinatesAttributeLocation, 2, GLES20.GL_FLOAT,
                false, 16, mFloatBuffer);
        GLES31.glEnableVertexAttribArray(textureCoordinatesAttributeLocation);
        mFloatBuffer.position(0);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN, 0, 6);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        if (!mCouldDraw) {
            return;
        }
        if (mBlurCount == 0) {
            if (mOnGLSurfaceActionListener != null) {
                mOnGLSurfaceActionListener.onDrawStarted();
            }
            surfaceTexture.updateTexImage();
        }

        doHori(mBlurCount == 0 ? textures[0] : vertFbo, getHeight(),
                BLUR_RADIUS, mBlurCount == 0);
        doVert(horiFbo, getWidth(), BLUR_RADIUS);
        mBlurCount++;

        GLES31.glUseProgram(programFinal);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programFinal, "u_Matrix"),
                1, false, pm, 0);

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, vertFbo);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programFinal, "mainTexture"), 0);

        getUniform(programFinal);
        if (mBlurCount < mMaxBlurCount) {
            requestRender();
        } else {
            if (mOnGLSurfaceActionListener != null) {
                mOnGLSurfaceActionListener.onDrawFinished();
            }
        }
        mCouldScreenShot = true;
    }

    public void setMaxBlurCount(int blurCount) {
        mMaxBlurCount = blurCount;
    }

    public void setRepeatScreenShot(boolean isReScreenShot) {
        this.mIsRepeatScreenShot = isReScreenShot;
    }

    public void setScreenShot(Bitmap screenShot) {
        if (screenShot == null) {
            return;
        }
        this.screenShot = screenShot;
        this.mUseExternalScreenShot = true;
    }

    public void setOnGLSurfaceActionListener(OnGLSurfaceActionListener listener) {
        this.mOnGLSurfaceActionListener = listener;
    }

    public interface OnGLSurfaceActionListener {
        default void onDrawStarted() {}

        default void onDrawFinished() {}

        default void onScreenShotFinished() {}
    }
}
