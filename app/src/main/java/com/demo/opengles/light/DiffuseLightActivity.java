package com.demo.opengles.light;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.world.control.DirectionControlView;
import com.demo.opengles.world.control.JumpControlView;
import com.demo.opengles.world.control.MoveControlView;
import com.demo.opengles.world.game.Ground;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DiffuseLightActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private SeekBar seekBar;

    private MoveControlView moveControlView;
    private DirectionControlView directionControlView;
    private JumpControlView jumpControlView;

    private World world = new World();
    private Ground ground = new Ground(activity);
    private DiffuseLightCube diffuseCube = new DiffuseLightCube(activity);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_diffuse);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setProgress(30);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                diffuseCube.setLightStrong(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        moveControlView = findViewById(R.id.move_control_view);
        moveControlView.setOnMoveListener(new MoveControlView.OnMoveListener() {
            @Override
            public void onMove(int deltaX, int deltaY) {
                world.moveXYChange(deltaX, deltaY);
            }
        });

        directionControlView = findViewById(R.id.direction_control_view);
        directionControlView.setOnDirectionListener(new DirectionControlView.OnDirectionListener() {
            @Override
            public void onDirection(int deltaX, int deltaY) {
                world.directionChange(deltaX, deltaY);
            }
        });

        jumpControlView = findViewById(R.id.jump_control_view);
        jumpControlView.setOnJumpListener(new JumpControlView.OnJumpListener() {
            @Override
            public void onJump(float progress) {
                world.moveZChange((int) (progress * 100));
            }
        });

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                ground.create();
                diffuseCube.create();

                world.eyeXYZ(-20, -20, 20);
                world.directionXYZ(10, 10, -10);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                ground.change(gl, width, height);
                diffuseCube.change(gl, width, height);

                diffuseCube.setScale(10, 10, 10);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();
                ground.draw(world.getMVPMatrix());

                diffuseCube.draw(world.getMVPMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

}
