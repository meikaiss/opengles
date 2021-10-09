package com.demo.opengles.record.camera2.glrecordconcat;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Size;
import android.view.Surface;

import com.demo.opengles.gaussian.render.CameraRenderObject;
import com.demo.opengles.gaussian.render.DefaultFitRenderObject;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.WaterMarkRenderObject;
import com.demo.opengles.util.OpenGLESUtil;

import javax.microedition.khronos.opengles.GL10;

public class CameraNode {

    private int cameraTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;

    private CameraRenderObject cameraRenderObject;
    private WaterMarkRenderObject waterMarkRenderObject;
    private DefaultFitRenderObject defaultFitRenderObject;

    private float[] vertexCoordinate;

    public int viewportX;
    public int viewportY;

    public boolean frameAvailable;

    public Surface getSurface() {
        return surface;
    }

    public void init(Activity activity, float[] vertexCoordinate) {
        cameraRenderObject = new CameraRenderObject(activity);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        waterMarkRenderObject = new WaterMarkRenderObject(activity);
        waterMarkRenderObject.isBindFbo = true;
        waterMarkRenderObject.isOES = false;
        defaultFitRenderObject = new DefaultFitRenderObject(activity);
        defaultFitRenderObject.isBindFbo = true;
        defaultFitRenderObject.isOES = false;

//        this.vertexCoordinate = vertexCoordinate;
    }

    public void onSurfaceCreate(GLSurfaceView glSurfaceView, Size previewSize) {
        cameraTextureId = OpenGLESUtil.createOesTexture();
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
        surface = new Surface(cameraSurfaceTexture);


        cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                        fpsUtil.trigger();
                frameAvailable = true;

                glSurfaceView.requestRender();
            }
        });

        cameraRenderObject.vertexCoordinate = vertexCoordinate;
        cameraRenderObject.onCreate();
        waterMarkRenderObject.onCreate();
        defaultFitRenderObject.onCreate();

        //设置Surface纹理的宽高，Camera2在预览时会选择宽高最相近的预览尺寸，将此尺寸的图像输送到Surface纹理中
        cameraSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        cameraRenderObject.inputWidth = previewSize.getWidth();
        cameraRenderObject.inputHeight = previewSize.getHeight();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height, int x, int y) {
        cameraRenderObject.onChange(cameraRenderObject.inputWidth, cameraRenderObject.inputHeight);
        waterMarkRenderObject.onChange(cameraRenderObject.inputWidth, cameraRenderObject.inputHeight);

        defaultFitRenderObject.inputWidth = waterMarkRenderObject.width;
        defaultFitRenderObject.inputHeight = waterMarkRenderObject.height;
        defaultFitRenderObject.viewportX = viewportX = x;
        defaultFitRenderObject.viewportY = viewportY = y;
        defaultFitRenderObject.onChange(width, height);
    }

    public void updateCoords(DefaultRenderObject defaultRenderObject,
                             int width, int height, int x, int y) {
        defaultRenderObject.width = width;
        defaultRenderObject.height = height;
        defaultRenderObject.viewportX = x;
        defaultRenderObject.viewportY = y;
    }

    public void onDrawFrame(GL10 gl, int id, DefaultRenderObject offScreen) {
//        if (!frameAvailable) {
//            return;
//        }
//        frameAvailable = false;
        cameraSurfaceTexture.updateTexImage();

        cameraRenderObject.onDraw(cameraTextureId);
        waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);

        defaultFitRenderObject.tag = id;
        defaultFitRenderObject.onDraw(waterMarkRenderObject.fboTextureId);

        offScreen.tag = id;
        offScreen.onDraw(defaultFitRenderObject.fboTextureId);
    }
}
