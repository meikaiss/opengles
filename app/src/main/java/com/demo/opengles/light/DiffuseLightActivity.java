package com.demo.opengles.light;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DiffuseLightActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private SeekBar seekBar;

    private World world = new World();
    private DiffuseLightCube cube = new DiffuseLightCube(activity);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_diffuse);

        glSurfaceView = findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setProgress(30);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cube.setLightStrong(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                cube.create();

                world.eyeXYZ(-20, -20, 20);
                world.directionXYZ(10, 10, -10);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                cube.change(gl, width, height);
                cube.setTranslate(0, 0, 0);
                cube.setScale(5, 5, 5);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();

                cube.draw(world.getMVPMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}
