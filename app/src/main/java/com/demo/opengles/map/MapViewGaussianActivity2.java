package com.demo.opengles.map;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.MapView;
import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.DefaultRenderObject;
import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.main.BaseActivity;

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
        setContentView(R.layout.activity_map_view_gaussian);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMap().setCustomRenderer(new CustomRenderer() {
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
                renderObjectH.onChange(width / 2, height / 2);
                renderObjectV.onChange(width / 2, height / 2);
                defaultRenderObject.onChange(width / 2, height / 2);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
//                renderObjectH.onDraw(7);
//                renderObjectV.onDraw(renderObjectH.fboTextureId);
                defaultRenderObject.onDraw(1);
            }
        });

    }


}
