package com.demo.opengles.map;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.MapView;
import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/15.
 */
public class MapViewGaussianActivity2 extends BaseActivity {

    private MapView mapView;

    private HVBlurRenderObject renderObjectH;
    private HVBlurRenderObject renderObjectV;
    private DefaultRenderObject defaultRenderObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_gaussian2);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMap().setCustomRenderer(new CustomRenderer() {
            int width;
            int height;

            @Override
            public void OnMapReferencechanged() {

            }

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                renderObjectH = new HVBlurRenderObject(MapViewGaussianActivity2.this);
                renderObjectH.setBlurRadius(30);
                renderObjectH.setBlurOffset(1, 0);
                renderObjectH.clearFlag = false;
                renderObjectH.isBindFbo = true;
                renderObjectH.onCreate();

                renderObjectV = new HVBlurRenderObject(MapViewGaussianActivity2.this);
                renderObjectV.setBlurRadius(30);
                renderObjectV.setBlurOffset(0, 1);
                renderObjectV.clearFlag = false;
                renderObjectV.isBindFbo = true;
                renderObjectV.onCreate();

                defaultRenderObject = new DefaultRenderObject(MapViewGaussianActivity2.this);
                defaultRenderObject.isBindFbo = false;
                defaultRenderObject.clearFlag = false;
                defaultRenderObject.onCreate();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                this.width = width;
                this.height = height;
                renderObjectH.onChange(width / 2, height / 2);
                renderObjectV.onChange(width / 2, height / 2);
                defaultRenderObject.onChange(width / 2, height / 2);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                int screenshotSize = width * height;
                ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
                bb.order(ByteOrder.nativeOrder());
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                        bb);
                int pixelsBuffer[] = new int[screenshotSize];
                bb.asIntBuffer().get(pixelsBuffer);
                bb = null;

                for (int i = 0; i < screenshotSize; ++i) {
                    // The alpha and green channels' positions are preserved while the      red
                    // and blue are swapped
                    pixelsBuffer[i] =
                            ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0, 0, width, height);

                int textureId = OpenGLESUtil.createBitmapTextureId(bitmap, GLES20.GL_TEXTURE0);
                bitmap.recycle();

                renderObjectH.onDraw(textureId);
                renderObjectV.onDraw(renderObjectH.fboTextureId);
                defaultRenderObject.onDraw(renderObjectV.fboTextureId);
            }
        });

    }


}
