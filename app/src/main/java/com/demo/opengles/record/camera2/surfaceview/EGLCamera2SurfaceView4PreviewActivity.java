package com.demo.opengles.record.camera2.surfaceview;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class EGLCamera2SurfaceView4PreviewActivity extends AppCompatActivity {

    private Camera2SurfaceViewPreviewManager recordManager1;
    private Camera2SurfaceViewPreviewManager recordManager2;
    private Camera2SurfaceViewPreviewManager recordManager3;
    private Camera2SurfaceViewPreviewManager recordManager4;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recordManager1.onDestroy();
        recordManager2.onDestroy();
        recordManager3.onDestroy();
        recordManager4.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_surfaceview_preview4);

        recordManager1 = new Camera2SurfaceViewPreviewManager();
        recordManager2 = new Camera2SurfaceViewPreviewManager();
        recordManager3 = new Camera2SurfaceViewPreviewManager();
        recordManager4 = new Camera2SurfaceViewPreviewManager();

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
        recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
        recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);

    }

}
