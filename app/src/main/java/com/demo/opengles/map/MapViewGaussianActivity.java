package com.demo.opengles.map;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.demo.opengles.R;
import com.demo.opengles.gaussian.view.GaussianConfig;
import com.demo.opengles.gaussian.view.GaussianGLSurfaceView;
import com.demo.opengles.gaussian.view.provider.IBitmapProvider;
import com.demo.opengles.main.BaseActivity;

/**
 * Created by meikai on 2021/10/15.
 */
public class MapViewGaussianActivity extends BaseActivity {

    private MapView mapView;
    private GaussianGLSurfaceView glSurfaceView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_gaussian);

        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        glSurfaceView = findViewById(R.id.gl_surface_view);
        GaussianConfig gaussianConfig = new GaussianConfig();
        gaussianConfig.blurRadius = 20;
        gaussianConfig.blurOffsetW = 2;
        gaussianConfig.blurOffsetH = 2;

        gaussianConfig.setBitmapCreateMode(GaussianConfig.BitmapCreateMode.GlEveryDraw);
        gaussianConfig.bitmapProvider = new IBitmapProvider() {
            @Override
            public Bitmap getOriginBitmap() {
                return bitmap;
            }
        };
        gaussianConfig.clipDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_svg_test_real);
        gaussianConfig.repeatCount = 0;
        glSurfaceView.setConfig(gaussianConfig);
        glSurfaceView.init();

        mapView.getMap().getMapScreenShot(onMapScreenShotListener);

        initGlSurfaceView();
    }

    private AMap.OnMapScreenShotListener onMapScreenShotListener = new AMap.OnMapScreenShotListener() {
        @Override
        public void onMapScreenShot(Bitmap bitmap) {
            MapViewGaussianActivity.this.bitmap = bitmap;

            mapView.post(new Runnable() {
                @Override
                public void run() {
                    mapView.getMap().getMapScreenShot(onMapScreenShotListener);
                }
            });

            glSurfaceView.requestRender();
        }

        @Override
        public void onMapScreenShot(Bitmap bitmap, int i) {
        }
    };

    private void initGlSurfaceView() {

    }

}
