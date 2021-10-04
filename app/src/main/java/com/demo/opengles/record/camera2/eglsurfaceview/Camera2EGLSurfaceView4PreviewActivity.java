package com.demo.opengles.record.camera2.eglsurfaceview;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

public class Camera2EGLSurfaceView4PreviewActivity extends BaseActivity {

    private Camera2EGLSurfaceViewPreviewManager previewManager1;
    private Camera2EGLSurfaceViewPreviewManager previewManager2;
    private Camera2EGLSurfaceViewPreviewManager previewManager3;
    private Camera2EGLSurfaceViewPreviewManager previewManager4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_eglsurfaceview_preview4);

        previewManager1 = new Camera2EGLSurfaceViewPreviewManager();
        previewManager2 = new Camera2EGLSurfaceViewPreviewManager();
        previewManager3 = new Camera2EGLSurfaceViewPreviewManager();
        previewManager4 = new Camera2EGLSurfaceViewPreviewManager();

        previewManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
        previewManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
        previewManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
        previewManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        previewManager1.onDestroy();
        previewManager2.onDestroy();
        previewManager3.onDestroy();
        previewManager4.onDestroy();
    }

}
