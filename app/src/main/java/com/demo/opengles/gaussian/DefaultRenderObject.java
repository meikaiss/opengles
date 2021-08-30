package com.demo.opengles.gaussian;

import android.content.Context;

/**
 * Created by meikai on 2021/08/29.
 */
public class DefaultRenderObject extends BaseRenderObject{

    public DefaultRenderObject(Context context) {
        super(context);
        initShaderFileName("render/base/base/vertex.frag", "render/base/base/frag.frag");
    }

}
