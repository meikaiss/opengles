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

    public boolean frameAvailable;

    public Surface getSurface() {
        return surface;
    }

    public void init(Activity activity) {
        cameraRenderObject = new CameraRenderObject(activity);
        cameraRenderObject.isBindFbo = true;
        cameraRenderObject.isOES = true;
        waterMarkRenderObject = new WaterMarkRenderObject(activity);
        waterMarkRenderObject.isBindFbo = true;
        waterMarkRenderObject.isOES = false;
        defaultFitRenderObject = new DefaultFitRenderObject(activity);
        defaultFitRenderObject.isBindFbo = true;
        defaultFitRenderObject.isOES = false;
    }

    public void onSurfaceCreate(GLSurfaceView glSurfaceView, Size previewSize) {
        cameraTextureId = OpenGLESUtil.createOesTexture();
        cameraSurfaceTexture = new SurfaceTexture(cameraTextureId);
        surface = new Surface(cameraSurfaceTexture);


        cameraSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //fpsUtil.trigger();
                frameAvailable = true;

                glSurfaceView.requestRender();
            }
        });

        cameraRenderObject.onCreate();
        waterMarkRenderObject.onCreate();
        defaultFitRenderObject.onCreate();

        //??????Surface??????????????????Camera2????????????????????????????????????????????????????????????????????????????????????Surface?????????
        cameraSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        cameraRenderObject.inputWidth = previewSize.getWidth();
        cameraRenderObject.inputHeight = previewSize.getHeight();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        cameraRenderObject.onChange(cameraRenderObject.inputWidth, cameraRenderObject.inputHeight);
        waterMarkRenderObject.onChange(cameraRenderObject.inputWidth, cameraRenderObject.inputHeight);

        defaultFitRenderObject.inputWidth = waterMarkRenderObject.width;
        defaultFitRenderObject.inputHeight = waterMarkRenderObject.height;
        defaultFitRenderObject.onChange(width, height);
    }

    public void onDrawFrame(GL10 gl, int id, DefaultRenderObject offScreen,
                            int width, int height, int x, int y) {

        if (!frameAvailable) {
            return;
        }
        frameAvailable = false;
        cameraSurfaceTexture.updateTexImage();

        cameraRenderObject.onDraw(cameraTextureId);
        waterMarkRenderObject.onDraw(cameraRenderObject.fboTextureId);

        defaultFitRenderObject.tag = id;
        defaultFitRenderObject.onDraw(waterMarkRenderObject.fboTextureId);

        offScreen.tag = id;
        offScreen.width = width;
        offScreen.height = height;
        offScreen.viewportX = x;
        offScreen.viewportY = y;
        offScreen.onDraw(defaultFitRenderObject.fboTextureId);
    }
}
