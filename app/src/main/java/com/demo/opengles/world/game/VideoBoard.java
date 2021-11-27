package com.demo.opengles.world.game;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

import com.demo.opengles.world.base.WorldObject;
import com.demo.opengles.world.common.OesTexturePlane;

import javax.microedition.khronos.opengles.GL10;

public class VideoBoard extends WorldObject {

    private OesTexturePlane oesTexturePlane;

    private MediaPlayer mediaPlayer;

    public VideoBoard(Context context) {
        super(context);
        oesTexturePlane = new OesTexturePlane(context);
        oesTexturePlane.setOnSurfacePrepareListener(new OesTexturePlane.OnSurfacePrepareListener() {
            @Override
            public void onSurfacePrepare(Surface surface) {
                try {
                    playVideo(surface);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void create() {
        oesTexturePlane.create();

    }

    @Override
    public void change(GL10 gl, int width, int height) {
        oesTexturePlane.change(gl, width, height);
    }

    @Override
    public void draw(float[] MVPMatrix) {
        setRotate(90, 1, 0, 0);
        setTranslate(0, 0, 3);

        oesTexturePlane.setRotate(90, 1, 0, 0);
        oesTexturePlane.setScale(10 * 1080f / 1920, 10, 10);
        oesTexturePlane.setTranslate(0, 10, 10);
        oesTexturePlane.draw(MVPMatrix);
    }

    @Override
    public void release() {
        super.release();
        oesTexturePlane.release();

        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void playVideo(Surface surface) throws Exception {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //将此MediaPlayer解码出的图像输出到此SurfaceHolder对应的Surface中，
        // 而这个Surface是由SurfaceView创建的，而SurfaceView在创建时已将此Surface挂载到系统屏幕抽象上，从而能显示到屏幕中
        mediaPlayer.setSurface(surface);
        //设置播放的视频源
        AssetFileDescriptor afd = context.getAssets().openFd("video/shotlocalvideo_1080x1920.mp4");
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.prepareAsync();
    }
}
