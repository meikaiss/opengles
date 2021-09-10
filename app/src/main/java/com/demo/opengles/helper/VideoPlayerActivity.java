package com.demo.opengles.helper;

import android.os.Bundle;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vider_player);

        videoView = findViewById(R.id.video_view);

        String path = getIntent().getStringExtra("path");
        videoView.setVideoPath(path);
        videoView.start();
    }


}
