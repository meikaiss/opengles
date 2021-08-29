package com.demo.opengles.gaussian;

/**
 * Created by meikai on 2021/08/29.
 */
public interface RenderAble {
    void onCreate();

    void onChange(int width, int height);

    void onDraw(int textureId);

    void onRelease();
}
