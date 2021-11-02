package com.demo.opengles.record.camera2.mediarecorder;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class MediaRecorderActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private MediaRecordManager mediaRecordManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_recorder);

        surfaceView = findViewById(R.id.surface_view);

        mediaRecordManager = new MediaRecordManager(this, 0, surfaceView);

        findViewById(R.id.btn_start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecordManager.startRecord();
            }
        });

        findViewById(R.id.btn_stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecordManager.stopRecord();
            }
        });
    }

}
