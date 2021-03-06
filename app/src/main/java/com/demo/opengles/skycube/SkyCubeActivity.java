package com.demo.opengles.skycube;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.egl.AntiConfigChooser;
import com.demo.opengles.main.BaseActivity;
import com.demo.opengles.world.control.DirectionControlView;
import com.demo.opengles.world.control.JumpControlView;
import com.demo.opengles.world.control.MoveControlView;

public class SkyCubeActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;

    private MoveControlView moveControlView;
    private DirectionControlView directionControlView;
    private JumpControlView jumpControlView;

    private SkyCubeRender render;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skycube);

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        initControlView();
        initTouchListener();

        render = new SkyCubeRender(this);
        glSurfaceView.setRenderer(render);

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    private void initControlView(){
        moveControlView = findViewById(R.id.move_control_view);
        moveControlView.setOnMoveListener(new MoveControlView.OnMoveListener() {
            @Override
            public void onMove(int deltaX, int deltaY) {
                render.getWorld().moveXYChange(deltaX, deltaY);
            }
        });

        directionControlView = findViewById(R.id.direction_control_view);
        directionControlView.setOnDirectionListener(new DirectionControlView.OnDirectionListener() {
            @Override
            public void onDirection(int deltaX, int deltaY) {
                render.getWorld().directionChange(deltaX, deltaY);
            }
        });

        jumpControlView = findViewById(R.id.jump_control_view);
        jumpControlView.setOnJumpListener(new JumpControlView.OnJumpListener() {
            @Override
            public void onJump(float progress) {
                render.getWorld().eyeZChange((int) (progress * 100));
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                /**
                 * ?????????Move???????????????scale??????????????????????????????Move???????????????????????????????????????true????????????????????????false???????????????
                 */
                float scaleFactor = detector.getScaleFactor();
                render.getWorld().onScale(scaleFactor);
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
