package com.demo.opengles.gaussian;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.DefaultOesRenderObject;
import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.gaussian.render.TwoTexFilterRenderObject;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.util.OpenGLESUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/04.
 */
public class GaussianVideoPlayerActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;

    private int oesTextureId;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private MediaPlayer mediaPlayer;

    private HVBlurRenderObject renderObjectH;
    private HVBlurRenderObject renderObjectV;
    private DefaultOesRenderObject defaultOesRenderObject;
    private TwoTexFilterRenderObject twoTexFilterRenderObject;

    private int drawableWidth;
    private int drawableHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gaussian_video_player_activity);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);

        drawableWidth = (int) (Math.random() * 400 + 400);
        drawableHeight = (int) (Math.random() * 400 + 400);

        initGlSurfaceView();
    }

    private void initGlSurfaceView() {
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                oesTextureId = OpenGLESUtil.createOesTexture();
                surfaceTexture = new SurfaceTexture(oesTextureId);

                surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        glSurfaceView.requestRender();
                    }
                });
                surface = new Surface(surfaceTexture);

                try {
                    playVideo(surface);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                defaultOesRenderObject = new DefaultOesRenderObject(GaussianVideoPlayerActivity.this);
                defaultOesRenderObject.isBindFbo = true;
                defaultOesRenderObject.isOES = true;

                renderObjectH = new HVBlurRenderObject(GaussianVideoPlayerActivity.this);
                renderObjectH.setBlurOffset(5, 0);
                renderObjectH.isBindFbo = true;

                renderObjectV = new HVBlurRenderObject(GaussianVideoPlayerActivity.this);
                renderObjectV.setBlurOffset(0, 5);
                renderObjectV.isBindFbo = true;

                twoTexFilterRenderObject = new TwoTexFilterRenderObject(GaussianVideoPlayerActivity.this,
                        AppCompatResources.getDrawable(GaussianVideoPlayerActivity.this, R.drawable.ic_bg_svg_tip_dialog),
                        drawableWidth, drawableHeight);
                twoTexFilterRenderObject.isBindFbo = false;


                defaultOesRenderObject.onCreate();
                renderObjectH.onCreate();
                renderObjectV.onCreate();
                twoTexFilterRenderObject.onCreate();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                defaultOesRenderObject.onChange(width, height);
                renderObjectH.onChange(width, height);
                renderObjectV.onChange(width, height);
                twoTexFilterRenderObject.onChange(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                surfaceTexture.updateTexImage();
                defaultOesRenderObject.onDraw(oesTextureId);
                renderObjectH.onDraw(defaultOesRenderObject.fboTextureId);
                renderObjectV.onDraw(renderObjectH.fboTextureId);

                twoTexFilterRenderObject.setTexture2Id(renderObjectV.fboTextureId);  //纹理2 为 全部高斯模糊图像
                twoTexFilterRenderObject.onDraw(defaultOesRenderObject.fboTextureId); //纹理1 为 原始清晰图像
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void playVideo(Surface surface) throws Exception {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //将此MediaPlayer解码出的图像输出到此SurfaceHolder对应的Surface中，
        // 而这个Surface是由SurfaceView创建的，而SurfaceView在创建时已将此Surface挂载到系统屏幕抽象上，从而能显示到屏幕中
        mediaPlayer.setSurface(surface);
        //设置播放的视频源
        AssetFileDescriptor afd = getAssets().openFd("video/shotlocalvideo_1080x1920.mp4");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
