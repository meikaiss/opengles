package com.demo.opengles.gaussian.render;

import android.content.Context;

/**
 * 将外界传入的纹理渲染到屏幕或离屏缓存上，不做任何额外的变换
 * Created by meikai on 2021/08/29.
 */
public class CameraRenderObject extends BaseRenderObject {

    public CameraRenderObject(Context context) {
        super(context);
        initShaderFileName("render/base/camera/vertex.frag", "render/base/camera/frag.frag");
    }

}
