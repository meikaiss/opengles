package com.demo.opengles.surface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opengles.R;
import com.demo.opengles.main.BaseActivity;

/**
 * 一般来说，每一个窗口在SurfaceFlinger服务中都对应有一个Layer，用来描述它的绘图表面。对于那些具有SurfaceView的窗口来说，每一个SurfaceView
 * 在SurfaceFlinger服务中还对应有一个独立的Layer或者LayerBuffer，用来单独描述它的绘图表面，以区别于它的宿主窗口的绘图表面。
 *
 * 无论是LayerBuffer，还是Layer，它们都是以LayerBase为基类的，也就是说，SurfaceFlinger服务把所有的LayerBuffer和Layer
 * 都抽象为LayerBase，因此就可以用统一的流程来绘制和合成它们的UI。
 *
 * 注意，用来描述SurfaceView的Layer或者LayerBuffer的Z轴位置是小于用来其宿主Activity窗口的Layer的Z
 * 轴位置的，但是前者会在后者的上面挖一个“洞”出来，以便它的UI可以对用户可见。实际上，SurfaceView在其宿主Activity窗口上所挖的“洞”只不过是在其宿主Activity
 * 窗口上设置了一块透明区域。
 */
public class SurfaceViewActivity extends BaseActivity {
    private static final String TAG = "SurfaceView";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        Log.e(TAG, "onCreate");

        surfaceView = findViewById(R.id.surface_view);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    private void init() {
        surfaceHolder = surfaceView.getHolder();

        /**
         * SurfaceView的双缓冲的机制非常消耗系统内存，Android规定SurfaceView不可见时，
         * 会立即销毁SurfaceView的SurfaceHolder，以达到节约系统资源的目的，
         * 所以需要利用SurfaceHolder的回调函数对SurfaceHolder进行维护。
         */
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            boolean hasDestroyed;

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                hasDestroyed = false;

                Log.e(TAG, "surfaceCreated");
                int width = getWindow().getDecorView().getWidth();
                float cx = width / 2;
                float cy = cx + 100;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        float radius = 300f;
                        float squareWidth = 200f;
                        while (true) {
                            if (hasDestroyed) {
                                Log.e(TAG, "Surface已被系统销毁，无法绘制，退出子线程");
                                return;
                            }

                            radius--;
                            if (radius <= 10) {
                                radius = 300;
                            }

                            squareWidth--;
                            if (squareWidth <= 10) {
                                squareWidth = 200;
                            }

                            try {
                                //获取一个Canvas对象，并锁定，得到的Canvas对象，此Canvas对象是Surface中一个成员
                                Log.e(TAG, "lockCanvas.start");
                                //在子线程中此lock方法会阻塞住，直到下一个编舞者绘制信号到来时，返回Surface对应的canvas对象
                                Canvas mCanvas = holder.lockCanvas();
                                Log.e(TAG, "lockCanvas.end");
                                Log.e(TAG, "在子线程中绘制图像的每一帧");
                                mCanvas.drawRGB(125, 125, 125);
                                mCanvas.drawCircle(cx, cy, radius, paint);
                                mCanvas.drawRect(
                                        (width - squareWidth) / 2,
                                        width * 1,
                                        (width - squareWidth) / 2 + squareWidth,
                                        width + squareWidth,
                                        paint);

                                //释放同步锁，并提交改变，将新的数据进行展示，同一时候Surface中相关数据会被丢失
                                holder.unlockCanvasAndPost(mCanvas);
                            } catch (Exception e) {
                                Log.e(TAG, "在子线程中绘制图像时异常=>" + e.getMessage());
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width,
                                       int height) {
                Log.e(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                hasDestroyed = true;
                //销毁时触发，一般不可见时就会销毁
                Log.e(TAG, "surfaceDestroyed");
            }
        });

        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }
}
