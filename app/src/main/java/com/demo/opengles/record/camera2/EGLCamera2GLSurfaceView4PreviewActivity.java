package com.demo.opengles.record.camera2;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class EGLCamera2GLSurfaceView4PreviewActivity extends AppCompatActivity {

    private Camera2GLSurfaceViewPreviewManager recordManager1;
    private Camera2GLSurfaceViewPreviewManager recordManager2;
    private Camera2GLSurfaceViewPreviewManager recordManager3;
    private Camera2GLSurfaceViewPreviewManager recordManager4;

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
        setContentView(R.layout.activity_egl_camera2_glsurfaceview_preview4);

        recordManager1 = new Camera2GLSurfaceViewPreviewManager();
        recordManager2 = new Camera2GLSurfaceViewPreviewManager();
        recordManager3 = new Camera2GLSurfaceViewPreviewManager();
        recordManager4 = new Camera2GLSurfaceViewPreviewManager();

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
        recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
        recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);

    }

}
