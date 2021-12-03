package com.demo.opengles.world;

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
import com.demo.opengles.world.common.Volume;
import com.demo.opengles.world.control.DirectionControlView;
import com.demo.opengles.world.control.JumpControlView;
import com.demo.opengles.world.control.MoveControlView;
import com.demo.opengles.world.game.Axis;
import com.demo.opengles.world.game.Ground;
import com.demo.opengles.world.game.VideoBoard;
import com.demo.opengles.world.game.World;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meikai on 2021/10/16.
 */
public class WorldActivity extends BaseActivity {

    private static final String TAG = "WorldActivity";

    private GLSurfaceView glSurfaceView;
    private MoveControlView moveControlView;
    private DirectionControlView directionControlView;
    private JumpControlView jumpControlView;

    private World world = new World();
    private Volume cube = new Volume(activity);
    private List<Volume> cubeList = new ArrayList<>();
    private Ground ground = new Ground(activity);
    private Axis axis = new Axis(activity);
    private VideoBoard videoBoard = new VideoBoard(activity);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world);

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
                world.eyeZChange((int) (progress * 100));
            }
        });

        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(new AntiConfigChooser());

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                world.create();
                cube.create();
                ground.create();
                axis.create();

                for (int i = 0; i < 4; i++) {
                    Volume cube = new Volume(activity);
                    cube.create();
                    cubeList.add(cube);
                }

                videoBoard.create();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                world.change(gl, width, height);
                cube.change(gl, width, height);
                ground.change(gl, width, height);
                axis.change(gl, width, height);

                for (int i = 0; i < 4; i++) {
                    cubeList.get(i).change(gl, width, height);
                }

                //正方体的边长是2，因为横坐标范围从-1到1的长度是2
                cubeList.get(0).setTranslate(4f, 0, 3);
                cubeList.get(1).setTranslate(-4f, 0, 0);
                cubeList.get(2).setTranslate(0, 4f, 0);
                cubeList.get(3).setTranslate(0, -4f, 0);

                videoBoard.change(gl, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                world.draw();

                axis.draw(world.getMVPMatrix());
                ground.draw(world.getMVPMatrix());
                cube.draw(world.getMVPMatrix());
                for (int i = 0; i < 4; i++) {
                    cubeList.get(i).draw(world.getMVPMatrix());
                }

                videoBoard.draw(world.getMVPMatrix());
            }
        });

        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        initTouchListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoBoard.release();
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
