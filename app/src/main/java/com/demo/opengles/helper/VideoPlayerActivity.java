package com.demo.opengles.helper;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.opengles.R;

import java.io.File;
import java.io.FileInputStream;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vider_player);

        videoView = findViewById(R.id.video_view);

        path = getIntent().getStringExtra("path");
        parseVideoInfo(path);
        videoView.setVideoPath(path);
        videoView.start();

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
            }
        });
        findViewById(R.id.btn_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });
        findViewById(R.id.btn_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseVideoInfo(path);
                videoView.seekTo(0);
                videoView.start();
            }
        });
    }

    private void parseVideoInfo(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            FileInputStream inputStream = new FileInputStream(new File(path).getAbsolutePath());
            retriever.setDataSource(inputStream.getFD());

            String duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)
            String width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
            String height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高

            Toast.makeText(VideoPlayerActivity.this, "时长=" + duration + "ms，宽="
                    + width + "，高=" + height, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.e("TAG", "MediaMetadataRetriever exception " + ex);
        } finally {
            retriever.release();
        }
    }

}
