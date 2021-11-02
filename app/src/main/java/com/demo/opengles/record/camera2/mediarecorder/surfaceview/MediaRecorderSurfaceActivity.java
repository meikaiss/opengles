package com.demo.opengles.record.camera2.mediarecorder.surfaceview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.TimeConsumeUtil;

public class MediaRecorderSurfaceActivity extends BaseActivity implements View.OnClickListener {
    private Button mBtnStart, mBtnFinish;

    private MediaRecorderSurfaceManager manager1;
    private MediaRecorderSurfaceManager manager2;
    private MediaRecorderSurfaceManager manager3;
    private MediaRecorderSurfaceManager manager4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediarecorder_camera1_surface);
        mBtnStart = findViewById(R.id.btn_start_record);
        mBtnFinish = findViewById(R.id.btn_stop_record);

        manager1 = new MediaRecorderSurfaceManager(activity, 0, findViewById(R.id.surface_view_1));
//        manager2 = new MediaRecorderSurfaceManager(activity, 1, findViewById(R.id.surface_view_2));
//        manager3 = new MediaRecorderSurfaceManager(activity, 2, findViewById(R.id.surface_view_3));
//        manager4 = new MediaRecorderSurfaceManager(activity, 3, findViewById(R.id.surface_view_4));

        mBtnStart.setOnClickListener(this);
        mBtnFinish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_record:
                TimeConsumeUtil.start("startRecorder");
                manager1.startRecorder();
//                manager2.startRecorder();
//                manager3.startRecorder();
//                manager4.startRecorder();
                TimeConsumeUtil.end("startRecorder");
                break;
            case R.id.btn_stop_record:
                TimeConsumeUtil.start("stopRecorder");
                manager1.stopRecorder();
//                manager2.stopRecorder();
//                manager3.stopRecorder();
//                manager4.stopRecorder();
                TimeConsumeUtil.end("stopRecorder");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TimeConsumeUtil.start("onDestroy");
        manager1.onDestroy();
//        manager2.onDestroy();
//        manager3.onDestroy();
//        manager4.onDestroy();
        TimeConsumeUtil.end("onDestroy");
    }

}