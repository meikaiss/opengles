package com.demo.opengles.light;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.OnSeekBarChangeListenerImpl;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AmbientLightActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private SeekBar seekBarStrong;
    private SeekBar seekBarR;
    private SeekBar seekBarG;
    private SeekBar seekBarB;

    private World world = new World();
    private AmbientLightCube cube = new AmbientLightCube(activity);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_ambient);

        glSurfaceView = findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        initSeekBar();

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

    private void initSeekBar() {
        seekBarStrong = findViewById(R.id.seek_bar);
        seekBarStrong.setProgress(30);
        seekBarStrong.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cube.setLightStrong(progress / 100f);
            }
        });

        seekBarR = findViewById(R.id.seek_bar_r);
        seekBarR.setProgress(100);
        seekBarR.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cube.setLightColorR(progress / 100f);
            }
        });

        seekBarG = findViewById(R.id.seek_bar_g);
        seekBarG.setProgress(100);
        seekBarG.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cube.setLightColorG(progress / 100f);
            }
        });

        seekBarB = findViewById(R.id.seek_bar_b);
        seekBarB.setProgress(100);
        seekBarB.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cube.setLightColorB(progress / 100f);
            }
        });
    }

}
