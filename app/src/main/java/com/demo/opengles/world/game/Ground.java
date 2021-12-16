package com.demo.opengles.world.game;

import android.content.Context;

import com.demo.opengles.world.base.WorldObject;
import com.demo.opengles.world.common.PlaneTexture;

import javax.microedition.khronos.opengles.GL10;

/**
 * 组合体不具体世界坐标变换特征，因为平移矩阵不支持相乘叠加
 */
public class Ground extends WorldObject {

    private PlaneTexture flat;

    public Ground(Context context) {
        super(context);
        flat = new PlaneTexture(context);
    }

    @Override
    public void create() {
        flat.create();
    }

    @Override
    public void change(GL10 gl, int width, int height) {
        flat.change(gl, width, height);
    }

    @Override
    public void draw(float[] MVPMatrix) {

        setTranslate(0, 0, -3);


        for (int i = -10; i <= 10; i++) {
            for (int j = -10; j <= 10; j++) {
                float width = 8;
                float scale = 4;
                flat.setTranslate(i * width + translateX, j * width, -1f + translateZ);
                flat.setScale(scale, scale, scale);
                flat.draw(MVPMatrix);
            }
        }
    }

}
