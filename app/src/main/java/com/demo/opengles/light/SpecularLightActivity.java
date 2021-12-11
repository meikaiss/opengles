package com.demo.opengles.light;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.sdk.OnSeekBarChangeListenerImpl;
import com.demo.opengles.world.common.Point;
import com.demo.opengles.world.control.DirectionControlView;
import com.demo.opengles.world.control.JumpControlView;
import com.demo.opengles.world.control.MoveControlView;
import com.demo.opengles.world.game.Axis;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SpecularLightActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;
    private SeekBar seekBar;
    private SeekBar seekBarXY;
    private TextView tvPosXY;

    private MoveControlView moveControlView;
    private DirectionControlView directionControlView;
    private JumpControlView jumpControlView;

    private World world = new World();
    private Point point = new Point(activity);
    private Axis axis = new Axis(activity);
    private SpecularLightCube specularCube = new SpecularLightCube(activity);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_specular);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        initTouchListener();

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setProgress(80);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                specularCube.setLightStrong(progress / 100f);
            }
        });

        seekBarXY = findViewById(R.id.seek_bar_xy);
        seekBarXY.setProgress(20);
        seekBarXY.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                specularCube.setLightPosXY(progress);
                tvPosXY.setText(String.valueOf(progress));
            }
        });

        tvPosXY = findViewById(R.id.tv_pos_xy);

        moveControlView = findViewById(R.id.move_control_view);
        moveControlView.setOnMoveListener(new MoveControlView.OnMoveListener() {
            @Override
            public void onMove(int deltaX, int deltaY) {
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    specularCube.addTranslate(deltaX > 0 ? 0.1f : -0.1f, 0, 0);
                } else {
                    specularCube.addTranslate(0, deltaY > 0 ? 0.1f : -0.1f, 0);
                }
            }
        });

        directionControlView = findViewById(R.id.direction_control_view);
        directionControlView.setOnDirectionListener(new DirectionControlView.OnDirectionListener() {
            @Override
            public void onDirection(int deltaX, int deltaY) {
                world.directionChange(deltaX, 0);
            }
        });

        jumpControlView = findViewById(R.id.jump_control_view);
        jumpControlView.setOnJumpListener(new JumpControlView.OnJumpListener() {
            @Override
            public void onJump(float progress) {
                world.eyeZChange((int) (progress * 100));
            }
        });

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                point.setVertexCoord(10f, 10f, 10f);
                point.setColor(1f, 1f, 1f, 1f);
                point.create();
                axis.create();
                specularCube.create();

                world.eyeXYZ(-5, -5, 5);
                world.setInitDirectionXYZ(5, 5, -5);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                point.change(gl, width, height);
                axis.change(gl, width, height);
                specularCube.change(gl, width, height);

//                specularCube.setScale(10f, 10f, 10f);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();
                point.draw2(gl, world.getMVPMatrix());
                axis.draw(world.getMVPMatrix());
                specularCube.draw(world.getProjectMatrix(), world.getViewMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                /**
                 * 每一次Move所计算出的scale是针对上一次消费掉的Move事件的触摸位置，此方法返回true表示已消费，返回false表示未消费
                 */
                float scaleFactor = detector.getScaleFactor();
                world.onScale(scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
        });
        scaleGestureDetector.setQuickScaleEnabled(true);

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return scaleGestureDetector.onTouchEvent(event);
            }
        });
    }

}