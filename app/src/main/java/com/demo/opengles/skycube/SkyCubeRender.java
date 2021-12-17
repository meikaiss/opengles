package com.demo.opengles.skycube;

import android.app.Activity;
import android.opengl.GLSurfaceView;

import com.demo.opengles.world.game.Axis;
import com.demo.opengles.world.game.World;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SkyCubeRender implements GLSurfaceView.Renderer {

    private World world = new World();
    private Axis axis;
    private SkyCube skyCube;

    public World getWorld() {
        return world;
    }

    public SkyCubeRender(Activity activity) {
        axis = new Axis(activity);
        skyCube = new SkyCube(activity);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        world.create();
        axis.create();
        skyCube.create();

        world.eyeXYZ(-20, -20, 20);
        world.setInitDirectionXYZ(20, 20, -20);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        world.change(gl, width, height);
        axis.change(gl, width, height);
        skyCube.change(gl, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        world.draw();
        axis.draw(world.getMVPMatrix());
        skyCube.draw(world.getMVPMatrix());
    }

}
