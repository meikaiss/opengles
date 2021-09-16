package com.demo.opengles.gaussian.pop.one;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.demo.opengles.R;
import com.demo.opengles.gaussian.render.HVBlurRenderObject;
import com.demo.opengles.gaussian.render.OneTexFilterRenderObject;
import com.demo.opengles.util.TimeConsumeUtil;
import com.demo.opengles.util.ViewUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 高斯模糊弹框的基类
 * 只负责处理不规则图形的高斯模糊，不负责具体的业务界面逻辑
 */
public abstract class AbsGaussianPop {

    protected Activity activity;
    protected PopupWindow mPopWindow;

    private View popupView;
    private GLSurfaceView glSurfaceView;

    private HVBlurRenderObject renderObjectH;
    private HVBlurRenderObject renderObjectV;
    private OneTexFilterRenderObject defaultRenderObject;

    /**
     * 弹框显示区域的宽度
     */
    protected abstract int getWidth();

    /**
     * 弹框显示区域的高度
     */
    protected abstract int getHeight();

    /**
     * 业务内容布局的资源id
     */
    protected abstract int getContentLayoutId();

    /**
     * 描述高斯模糊范围的矢量Drawable
     */
    protected abstract VectorDrawable getVectorDrawable();

    protected <T extends View> T findViewById(int id) {
        return popupView.findViewById(id);
    }

    public AbsGaussianPop(Activity activity) {
        this.activity = activity;

        popupView = LayoutInflater.from(this.activity).inflate(R.layout.pop_window_gaussian, null);

        LayoutInflater.from(this.activity).inflate(getContentLayoutId(), (ViewGroup) popupView, true);

        mPopWindow = new PopupWindow(popupView, getWidth(), getHeight());

        glSurfaceView = popupView.findViewById(R.id.gl_surface_view);

        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.setFocusable(true);
    }

    private int[] location;
    private int offsetX;
    private View anchor;

    public AbsGaussianPop show(View anchor) {
        this.anchor = anchor;
        offsetX = -(getWidth() - anchor.getWidth()) / 2;

        location = new int[2];
        anchor.getLocationOnScreen(location);
        location[1] -= ViewUtil.getStatusBarHeight(activity);

        initSurfaceView();

        mPopWindow.showAsDropDown(anchor, offsetX, 0);
        return this;
    }

    private Bitmap screenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();

        view.buildDrawingCache();

        //状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int stateBarHeight = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        //获取屏幕宽高
        int widths = display.getWidth();
        int height = display.getHeight();

        //设置允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        //去掉状态栏高度、底部导航栏的高度
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, stateBarHeight, widths,
                height);


        view.destroyDrawingCache();

        return bitmap;
    }

    private void initSurfaceView() {
        glSurfaceView.setEGLContextClientVersion(2);
        //支持背景透明-start
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
//        glSurfaceView.setZOrderOnTop(true);
        //支持背景透明-end

        renderObjectH = new HVBlurRenderObject(activity);
        renderObjectH.setBlurOffset(5, 0);
        renderObjectH.isBindFbo = true;

        renderObjectV = new HVBlurRenderObject(activity);
        renderObjectV.setBlurOffset(0, 5);
        renderObjectV.isBindFbo = true;

        defaultRenderObject = new OneTexFilterRenderObject(activity, getVectorDrawable());
        defaultRenderObject.isBindFbo = false;

        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                glSurfaceView.requestRender();
            }
        });
    }

    private GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        private int textureId;
        private Bitmap bgBitmap;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            renderObjectH.onCreate();
            renderObjectV.onCreate();
            defaultRenderObject.onCreate();

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            renderObjectH.onChange(width, height);
            renderObjectV.onChange(width, height);
            defaultRenderObject.onChange(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            bgBitmap = screenShot(activity);

            bgBitmap = Bitmap.createBitmap(bgBitmap,
                    location[0] + offsetX,
                    location[1] + anchor.getHeight(),
                    getWidth(),
                    getHeight());

            textureId = createTexture();

            renderObjectH.onDraw(textureId);
            renderObjectV.onDraw(renderObjectH.fboTextureId);
            defaultRenderObject.onDraw(renderObjectV.fboTextureId);

            TimeConsumeUtil.calc("Gaussian.show");
        }

        private int createTexture() {
            int[] texture = new int[1];

            ////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////
            //在显卡的纹理硬件组上选择当前活跃的纹理单元为：第0号纹理单元，默认为0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //从offset=0号纹理单元开始生成n=1个纹理，并将纹理id保存到int[]=texture数组中
            GLES20.glGenTextures(1, texture, 0);
            textureId = texture[0];
            //将生成的纹理与gpu关联为2d纹理类型，传入纹理id作为参数，每次bing之后，后续操作的纹理都是该纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);

            //给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bgBitmap, 0);

            //返回生成的纹理的句柄
            return texture[0];
        }
    };
}
