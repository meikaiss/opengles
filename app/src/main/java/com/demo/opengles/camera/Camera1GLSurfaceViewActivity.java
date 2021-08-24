package com.demo.opengles.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

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

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class Camera1GLSurfaceViewActivity extends AppCompatActivity {

    private static final String TAG = "Camera1GLSurfaceViewAct";

    private final String vertexShader =
            "uniform mat4 vMatrix;" +
                    "attribute vec4 aPosition;\n"
                    + "attribute vec2 aTexCoord;\n"
                    + "varying vec2 vTexCoord;\n"
                    + "void main(void) {\n"
                    + "  gl_Position = vMatrix*aPosition;\n"
                    + "  vTexCoord = aTexCoord;\n"
                    + "}\n";

    private final String fragmentShader_1 =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "uniform samplerExternalOES sTexture;\n"
                    + "varying vec2 vTexCoord;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = texture2D(sTexture, vTexCoord);"
                    + "  gl_FragColor.r = 0.1;"
                    + "}\n";

    //顶点坐标
    private final float[] vertexCoords_1 = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    //纹理坐标-正放图片，纹理坐标与顶点坐标出现的顺序完全相同，则可以呈现出正放的图片
    //全部范围的纹理
    private final float[] textureCoord_1 = {
            0.0f, 0.0f, //左上、原点
            0.0f, 1.0f, //左下
            1.0f, 0.0f, //右上
            1.0f, 1.0f, //右下
    };

    private GLSurfaceView glSurfaceView;
    private VideoRender render;
    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1_glsurface_view);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        render = new VideoRender();
        {
            RenderObject renderObject = new RenderObject();
            renderObject.init(vertexShader, fragmentShader_1, vertexCoords_1, textureCoord_1,
                    true);
            renderObject.onSurfaceListener = new OnSurfaceListener() {
                @Override
                public void onSurfaceCreated(SurfaceTexture surfaceTexture, Surface surface) {
                    try {
                        initCamera1(surfaceTexture, surface);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            render.put(renderObject);
        }

        glSurfaceView.setRenderer(render);

        //必须在setRenderer之后才能调用
        //因为MediaPlayer会连续不断的输出最新的解码图像，所以必须持续不断的刷新opengl
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initCamera1(SurfaceTexture surfaceTexture, Surface surface) throws Exception {
        Log.e(TAG, "surfaceView.width=" + glSurfaceView.getWidth()
                + ", surfaceView.height=" + glSurfaceView.getHeight());

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            return;
        }
        int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        int degree = computeDegrees(cameraId);
        Log.e(TAG, "相机预览在此界面显示时，需要旋转的角度 = " + degree);
        camera.setDisplayOrientation(degree);

        //设置相机参数
        Camera.Parameters parameters = camera.getParameters();
        //系统特性：拍照的聚焦频率要高于拍视频
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(100);
        CollectUtil.execute(parameters.getSupportedPreviewFormats(),
                new CollectUtil.Executor<Integer>() {
                    @Override
                    public void execute(Integer integer) {
                        Log.e(TAG, "SupportedPreviewFormats: integer = " + integer);
                    }
                });

        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        CollectUtil.execute(sizeList, new CollectUtil.Executor<Camera.Size>() {
            @Override
            public void execute(Camera.Size size) {
                Log.e(TAG, "SupportedPreviewSizes: size.width = "
                        + size.width + " , size.height = " + size.height);
            }
        });
        Camera.Size size = adjustSurfaceViewWidthHeight(sizeList);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);

        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    //根据屏幕的旋转角度、相机的硬件内置放置角度，来设置显示旋转角度
    private int computeDegrees(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.e(TAG, "DefaultDisplay.Rotation = " + rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //前置相机，算法由谷歌提供
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //后置相机
            result = (cameraInfo.orientation - degrees) % 360;
        } else {
            //未知方向的相机，通常安卓手机上只有前后两个摄像头，其它智能设备如车机可能存在多个摄像头
            result = (cameraInfo.orientation - degrees) % 360;
        }

        return result;
    }

    /**
     * 相机硬件支持一系列固定的宽高的图像预览数据，而屏幕View可以被开发者设置成任意宽高，
     * 那么必然存在预览数据宽高比例与界面View的宽高比例不相等，若要保持View不变，则必然存在图像比例变形。
     * <p>
     * 预览比例与View比例的选择理论依据：
     * 1、在保持界面View完全填充的情况，尽量选择宽高比接近View的预览宽高比，允许弱微的变形
     */
    private Camera.Size adjustSurfaceViewWidthHeight(List<Camera.Size> sizeList) {
        float ASPECT_TOLERANCE = 0.1f;

        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);
            float cameraSizeScale = (float) cameraWidth / cameraHeight;
            float surfaceViewSizeScale =
                    (float) glSurfaceView.getWidth() / glSurfaceView.getHeight();

            if (Math.abs(cameraSizeScale - surfaceViewSizeScale) < ASPECT_TOLERANCE) {
                Log.e(TAG, "按顺序查找，找到比例小于阈值的预览size =" + size.width + ", " + size.height);
                return size;
            }
        }

        int minHeightDiff = Integer.MAX_VALUE;
        Camera.Size targetSize = null;
        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);

            int cameraWidth = Math.min(size.width, size.height);
            int cameraHeight = Math.max(size.width, size.height);

            int diff = Math.abs(glSurfaceView.getHeight() - cameraHeight);
            if (diff < minHeightDiff) {
                targetSize = size;
                minHeightDiff = diff;
            }
        }

        Log.e(TAG, "找到高度差值最小的size =" + targetSize.width + ", " + targetSize.height);
        return targetSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.release();
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private interface OnSurfaceListener {
        void onSurfaceCreated(SurfaceTexture surfaceTexture, Surface surface);
    }

    private static class RenderObject {
        private static int BYTE_OF_FLOAT = 4; //float类型数据所占用的字节数
        private static int COORDS_PER_VERTEX = 2; //每个顶点的坐标有2个float组成

        private String vertexShaderCode;
        private String fragmentShaderCode;

        private float[] vertexCoords;
        private float[] textureCoord;

        public boolean isEffective;

        //顶点个数
        private int vertexCount;
        //每个顶点坐标字节Buffer的步长，即多少个字节表示一个顶点坐标
        private int vertexStride;

        private int mProgram;
        private FloatBuffer vertexBuffer;
        private FloatBuffer textureCoordBuffer;
        private int vertexShaderIns;
        private int fragmentShaderIns;

        private int mMatrixHandler;
        private int glAPosition;
        private int glATexCoord;

        private float[] mViewMatrix = new float[16];
        private float[] mProjectMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];

        private int textureId;
        private SurfaceTexture surfaceTexture;
        private Surface surface;

        private boolean newFrameAvailable;

        public OnSurfaceListener onSurfaceListener;

        public void init(String vertexShaderCode, String fragmentShaderCode,
                         float[] vertexCoords, float[] textureCoord, boolean isEffective) {
            this.vertexShaderCode = vertexShaderCode;
            this.fragmentShaderCode = fragmentShaderCode;
            this.vertexCoords = vertexCoords;
            this.textureCoord = textureCoord;
            this.isEffective = isEffective;

            vertexCount = vertexCoords.length / COORDS_PER_VERTEX;
            vertexStride = COORDS_PER_VERTEX * BYTE_OF_FLOAT;

            //将内存中的顶点坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
            ByteBuffer bb = ByteBuffer.allocateDirect(vertexCoords.length * BYTE_OF_FLOAT);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertexCoords);
            vertexBuffer.position(0);

            //将内存中的纹理坐标数组，转换为字节缓冲区，因为opengl只能接受整块的字节缓冲区的数据
            ByteBuffer cc = ByteBuffer.allocateDirect(textureCoord.length * BYTE_OF_FLOAT);
            cc.order(ByteOrder.nativeOrder());
            textureCoordBuffer = cc.asFloatBuffer();
            textureCoordBuffer.put(textureCoord);
            textureCoordBuffer.position(0);
        }

        public void createProgram() {
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

            mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
            glAPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
            glATexCoord = GLES20.glGetAttribLocation(mProgram, "aTexCoord");

            createTexture();

            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    Log.e("tag", "onFrameAvailable, " + Thread.currentThread().getName());
                    newFrameAvailable = true;
                }
            });

            surface = new Surface(surfaceTexture);

            if (onSurfaceListener != null) {
                onSurfaceListener.onSurfaceCreated(surfaceTexture, surface);
            }
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //计算宽高比
            float ratio = (float) width / height;
            //设置透视投影
            Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1, 5);
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 2.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        }

        public void onDrawFrame(GL10 gl) {
            if (!isEffective) {
                return;
            }
            Log.e("tag", "onDrawFrame, " + Thread.currentThread().getName());
            if (newFrameAvailable) {
                //将图像数据流的最新图像更新到纹理中，那么后续opengl纹理渲染时就会显示出图像数据流的最新图像到屏幕上
                surfaceTexture.updateTexImage();
                newFrameAvailable = false;
            }

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_BLEND);

            GLES20.glUseProgram(mProgram);

            float[] matrixResult = new float[16];
            for (int i = 0; i < matrixResult.length; i++) {
                matrixResult[i] = mMVPMatrix[i];
            }

            float[] mMatrixRotate = new float[16];
            Matrix.setIdentityM(mMatrixRotate, 0);
            Matrix.rotateM(mMatrixRotate, 0, -90,
                    0, 0, 1);
            if (mMatrixRotate != null) {
                Matrix.multiplyMM(matrixResult, 0, matrixResult, 0, mMatrixRotate, 0);
            }

            //指定vMatrix的值
            GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, matrixResult, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

            GLES20.glVertexAttribPointer(glAPosition, 2, GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            GLES20.glEnableVertexAttribArray(glAPosition);

            GLES20.glVertexAttribPointer(glATexCoord, 2, GLES20.GL_FLOAT, false,
                    vertexStride, textureCoordBuffer);
            GLES20.glEnableVertexAttribArray(glATexCoord);

            int drawVertexCount = 4; //矩形的绘制需要4个顶点以三角形带的方式绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, drawVertexCount);
            GLES20.glFinish();
        }

        public int loadShader(int type, String shaderCode) {
            //根据type创建顶点着色器或者片元着色器
            int shader = GLES20.glCreateShader(type);
            //将资源加入到着色器中，并编译
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        private int createTexture() {
            int[] texture = new int[1];
            //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
            GLES20.glGenTextures(1, texture, 0);
            textureId = texture[0];
            //将生成的纹理与gpu关联为外部纹理类型，传入纹理id作为参数，每次bind之后，后续操作的纹理都是该纹理
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            //返回生成的纹理的句柄
            return texture[0];
        }

    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static class VideoRender implements GLSurfaceView.Renderer {

        public List<RenderObject> renderObjectList = new ArrayList<>();

        public void put(RenderObject object) {
            renderObjectList.add(object);
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
