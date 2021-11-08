package com.demo.opengles.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class OpenGLESUtil {
    private static final String TAG = "OpenGLESUtils";

    /**
     * 获取Shader代码
     */
    public static String getShaderCode(Context context, String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 构建FloatBuffer
     */
    public static FloatBuffer createFloatBuffer(float[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data, 0, data.length).position(0);
        return buffer;
    }

    /**
     * 加载Shader
     */
    public static int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }

    /**
     * 链接Program
     */
    public static int linkProgram(int vertexShader, int fragShader) {
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    /**
     * 获取Program
     */
    public static int getProgram(Context context, String vertexFilename, String fragFilename) {
        String vertexCode = getShaderCode(context, vertexFilename);
        String fragCode = getShaderCode(context, fragFilename);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragCode);
        return linkProgram(vertexShader, fragShader);
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        // 1. load shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == GLES20.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ");
            return GLES20.GL_NONE;
        }
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == GLES20.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ");
            return GLES20.GL_NONE;
        }
        // 2. create gl program
        int program = GLES20.glCreateProgram();
        if (program == GLES20.GL_NONE) {
            Log.e(TAG, "create program failed! ");
            return GLES20.GL_NONE;
        }
        // 3. attach shader
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // we can delete shader after attach
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        // 4. link program
        GLES20.glLinkProgram(program);
        // 5. check link status
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES20.GL_FALSE) { // link failed
            Log.e(TAG, "Error link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program); // delete program
            return GLES20.GL_NONE;
        }
        return program;
    }

    public static int createTextureId(Buffer buffer, int width, int height) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, buffer);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    public static int createTextureId2(Buffer buffer, int width, int height) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
                GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, buffer);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    //在显卡纹理硬件组的指定引脚上挂载一个纹理，并将Bitmap填充到此纹理中
    public static int createBitmapTextureId(Bitmap bitmap, int glTextureIndex) {
        int[] texture = new int[1];

        ////////////////////////////////////////////////////////////////////////////////
        //在显卡的纹理硬件组上选择当前活跃的纹理单元为：第0号纹理单元，默认为0
        GLES20.glActiveTexture(glTextureIndex);
        //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
        GLES20.glGenTextures(1, texture, 0);
        //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        //返回生成的纹理的句柄
        return texture[0];
    }

    /**
     * 创建外部纹理（OES）
     */
    public static int createOesTexture() {
        int[] textures = new int[1];
        //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
        GLES20.glGenTextures(1, textures, 0);
        //将生成的纹理与gpu关联为外部纹理类型，传入纹理id作为参数，每次bind之后，后续操作的纹理都是该纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //从当前启用的纹理引脚上解绑挂载的纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return textures[0];
    }

    /**
     * 创建FBO
     */
    public static int[] getFbo(int width, int height) {
        int[] fboData = new int[2];

        int fboId, fboTextureId;

        int[] fboIds = new int[1];
        GLES20.glGenFramebuffers(1, fboIds, 0);
        fboId = fboIds[0];

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTextureId, 0);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "glFramebufferTexture2D error");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        fboData[0] = fboId;
        fboData[1] = fboTextureId;
        return fboData;
    }

    /**
     * 创建VBO
     */
    public static int getVbo(FloatBuffer vertexBuffer, FloatBuffer coordinateBuffer) {
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        int vboId = vbos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                vertexBuffer.limit() * 4 + coordinateBuffer.limit() * 4,
                null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0,
                vertexBuffer.limit() * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.limit() * 4,
                coordinateBuffer.limit() * 4, coordinateBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        return vboId;
    }

    /**
     * 获取变换矩阵
     */
    public static float[] getMatrix(int width, int height, int oesW, int oesH) {
        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        float[] mMVPMatrix = new float[16];

        int w = oesW;
        int h = oesH;
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        return mMVPMatrix;
    }

    public static ExifInterface getExifInterfaceFromPath(String path) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exifInterface;
    }

    public static ExifInterface getExifInterfaceFromIs(InputStream is) {
        ExifInterface exifInterface = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exifInterface = new ExifInterface(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exifInterface;
    }

    /**
     * 获取图片的旋转方向
     */
    public static int getImageOrientation(ExifInterface exifInterface) {
        int degree = 0;
        if (exifInterface == null) {
            return degree;
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                degree = 0;
        }
        Log.d(TAG, "getImageOrientation: degree:" + degree);
        return degree;
    }

    /**
     * 是否是单位矩阵
     */
    public static boolean isIdentityM(float[] matrix) {
        float[] identityM = new float[16];
        Matrix.setIdentityM(identityM, 0);
        int length = matrix.length;
        for (int i = 0; i < length; i++) {
            float m = matrix[i];
            if (m != identityM[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取矩形的纹理坐标->原点在左上角
     */
    public static FloatBuffer getSquareCoordinateBuffer() {
        float[] data = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };
        return createFloatBuffer(data);
    }

    /**
     * 获取矩形的纹理坐标（倒立）->原点在左下角
     */
    public static FloatBuffer getSquareCoordinateReverseBuffer() {
        float[] data = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };
        return createFloatBuffer(data);
    }

    /**
     * 获取矩形的顶点坐标
     */
    public static FloatBuffer getSquareVertexBuffer() {
        float[] data = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, 1.0f,
                1.0f, -1.0f
        };
        return createFloatBuffer(data);
    }

    /**
     * 获取水印顶点坐标
     */
    public static FloatBuffer getContainWatermarkSquareVertexBuffer(float offsetX, float offsetY, float width, float height) {
        Log.d(TAG, "getContainWatermarkSquareVertexBuffer: offset x:" + offsetX + " y:" + offsetY + " width:" + width + " height:" + height);
        float[] data = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, 1.0f,
                1.0f, -1.0f,

                offsetX, offsetY,
                offsetX, offsetY - height,
                offsetX + width, offsetY,
                offsetX + width, offsetY - height
        };
        return createFloatBuffer(data);
    }

    /**
     * 获取球的纹理坐标
     *
     * @param n 偏转个数
     */
    public static FloatBuffer getBallCoordinateBuffer(int n) {
        List<Float> coordinateList = new ArrayList<>();

        int width = n;
        int height = n / 2;

        float dw = 1.0f / width;
        float dh = 1.0f / height;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float s = j * dw;
                float t = i * dh;

                coordinateList.add(s);
                coordinateList.add(t);

                coordinateList.add(s);
                coordinateList.add(t + dh);

                coordinateList.add(s + dw);
                coordinateList.add(t);

                coordinateList.add(s + dw);
                coordinateList.add(t + dh);
            }
        }
        float[] coordinateData = new float[coordinateList.size()];
        for (int i = 0; i < coordinateList.size(); i++) {
            coordinateData[i] = coordinateList.get(i);
        }
        return createFloatBuffer(coordinateData);
    }

    /**
     * 获取球的顶点坐标
     *
     * @param n 偏转个数
     */
    public static FloatBuffer getBallVertexBuffer(int n) {
        List<Float> vertexList = new ArrayList<>();

        float radius = 1;
        float angleSpan = 360f / n;

        for (float vAngle = 90; vAngle > -90; vAngle = vAngle - angleSpan) {
            for (float hAngle = 360; hAngle > 0; hAngle = hAngle - angleSpan) {
                double xozLength = radius * cos(radian(vAngle));
                float x1 = (float) (xozLength * cos(radian(hAngle)));
                float z1 = (float) (xozLength * sin(radian(hAngle)));
                float y1 = (float) (radius * sin(radian(vAngle)));

                xozLength = radius * cos(radian(vAngle - angleSpan));
                float x2 = (float) (xozLength * cos(radian(hAngle)));
                float z2 = (float) (xozLength * sin(radian(hAngle)));
                float y2 = (float) (radius * sin(radian(vAngle - angleSpan)));

                xozLength = radius * cos(radian(vAngle - angleSpan));
                float x3 = (float) (xozLength * cos(radian(hAngle - angleSpan)));
                float z3 = (float) (xozLength * sin(radian(hAngle - angleSpan)));
                float y3 = (float) (radius * sin(radian(vAngle - angleSpan)));

                xozLength = radius * cos(radian(vAngle));
                float x4 = (float) (xozLength * cos(radian(hAngle - angleSpan)));
                float z4 = (float) (xozLength * sin(radian(hAngle - angleSpan)));
                float y4 = (float) (radius * sin(radian(vAngle)));

                vertexList.add(x1);
                vertexList.add(y1);
                vertexList.add(z1);

                vertexList.add(x2);
                vertexList.add(y2);
                vertexList.add(z2);

                vertexList.add(x4);
                vertexList.add(y4);
                vertexList.add(z4);

                vertexList.add(x3);
                vertexList.add(y3);
                vertexList.add(z3);
            }
        }

        float[] vertexData = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertexData[i] = vertexList.get(i);
        }
        return createFloatBuffer(vertexData);
    }

    private static double cos(double radian) {
        return Math.cos(radian);
    }

    private static double sin(double radian) {
        return Math.sin(radian);
    }

    private static double radian(float angle) {
        return Math.toRadians(angle);
    }

    public static Bitmap createTextImage(String text, int textSize, String textColor, String bgColor, int padding) {

        Paint paint = new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float width = paint.measureText(text, 0, text.length());

        float top = paint.getFontMetrics().top;
        float bottom = paint.getFontMetrics().bottom;

        Bitmap bm = Bitmap.createBitmap((int) (width + padding * 2), (int) ((bottom - top) + padding * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        canvas.drawColor(Color.parseColor(bgColor));
        canvas.drawText(text, padding, -top + padding, paint);
        return bm;
    }

    /**
     * 获取贴纸Bitmap
     */
    public static Bitmap getStickerBitmap(Context context, int width, int height, int rawId) {
        Bitmap stickerBitmap = BitmapFactory.decodeStream(context.getResources().openRawResource(rawId));

        if (stickerBitmap.getWidth() > width) {
            float scale = (float) width / stickerBitmap.getWidth();

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postScale(scale, scale);

            stickerBitmap = Bitmap.createBitmap(stickerBitmap,
                    0, 0, stickerBitmap.getWidth(), stickerBitmap.getHeight(),
                    matrix, true);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(stickerBitmap, 0, 0, null);
        canvas.save();

        return bitmap;
    }

    /**
     * 保存Bitmap（png方式保存）
     */
    public static boolean saveBitmapForPng(String path, Bitmap bitmap) {
        return saveBitmap(path, bitmap, Bitmap.CompressFormat.PNG);
    }

    /**
     * 保存Bitmap（jpeg方式保存）
     */
    public static boolean saveBitmapForJpeg(String path, Bitmap bitmap) {
        return saveBitmap(path, bitmap, Bitmap.CompressFormat.JPEG);
    }

    public static boolean saveBitmap(String path, Bitmap bitmap, Bitmap.CompressFormat format) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(format, 100, fos);
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "saveBitmap: ", e);
        }
        return false;
    }

    public static boolean deleteTextureId(int textureId) {
        if (textureId <= 0) {
            return false;
        }
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        return true;
    }
}
