package com.demo.opengles.record.camera2;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class EGLCamera2Record4SameTimeActivity extends AppCompatActivity {

    private Button btnRecordStart;
    private Button btnRecordStop;
    private Button btnRecordPlay;

    private Camera2RecordManager recordManager1;
    private Camera2RecordManager recordManager2;
    private Camera2RecordManager recordManager3;
    private Camera2RecordManager recordManager4;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_camera2_record4_same_time);

        btnRecordStart = findViewById(R.id.btn_start_record);
        btnRecordStop = findViewById(R.id.btn_stop_record);
        btnRecordPlay = findViewById(R.id.btn_play_record);

        recordManager1 = new Camera2RecordManager();
//        recordManager2 = new Camera2RecordManager();
//        recordManager3 = new Camera2RecordManager();
//        recordManager4 = new Camera2RecordManager();

        recordManager1.create(this, findViewById(R.id.egl_surface_view_1), 0);
//        recordManager2.create(this, findViewById(R.id.egl_surface_view_2), 1);
//        recordManager3.create(this, findViewById(R.id.egl_surface_view_3), 2);
//        recordManager4.create(this, findViewById(R.id.egl_surface_view_4), 3);

    }

}
