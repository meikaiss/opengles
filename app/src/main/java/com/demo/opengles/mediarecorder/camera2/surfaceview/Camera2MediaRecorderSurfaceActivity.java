package com.demo.opengles.mediarecorder.camera2.surfaceview;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

public class Camera2MediaRecorderSurfaceActivity extends BaseActivity {

    private Camera2MediaRecorderSurfaceManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediarecorder_camera2_surface);

        manager = new Camera2MediaRecorderSurfaceManager(this,
                0, findViewById(R.id.surface_view_1));

        findViewById(R.id.btn_start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.startRecorder();
            }
        });

        findViewById(R.id.btn_stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.stopRecorder();
            }
        });
    }

}
