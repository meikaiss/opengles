package com.demo.opengles.world.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 方向控制器
 */
public class DirectionControlView extends View {

    private Paint paint;
    private int bigCX;
    private int bigCY;
    private int smallCX;
    private int smallCY;
    private float bigRadius;
    private float smallRadius = 30f;

    public OnDirectionListener onDirectionListener;

    public interface OnDirectionListener {
        void onDirection(int deltaX, int deltaY);
    }

    public void setOnDirectionListener(OnDirectionListener onDirectionListener) {
        this.onDirectionListener = onDirectionListener;
    }

    public DirectionControlView(Context context) {
        super(context);
        init();
    }

    public DirectionControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFA00000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        bigCX = getMeasuredWidth() / 2;
        bigCY = getMeasuredHeight() / 2;

        smallCX = getMeasuredWidth() / 2;
        smallCY = getMeasuredHeight() / 2;

        bigRadius = getMeasuredWidth() / 2 - 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(bigCX, bigCY, bigRadius, paint);
        canvas.drawCircle(smallCX, smallCY, smallRadius, paint);
    }

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            invalidate();
            triggerMoveListener();
            handler.sendEmptyMessage(0);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handler.sendEmptyMessage(0);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            float deltaX = bigCX - event.getX();
            float deltaY = bigCY - event.getY();
            float distance = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            if (distance + smallRadius > bigRadius) {
                smallCX = -(int) (deltaX * (bigRadius - smallRadius) / distance) + bigCX;
                smallCY = -(int) (deltaY * (bigRadius - smallRadius) / distance) + bigCY;
            } else {
                smallCX = (int) event.getX();
                smallCY = (int) event.getY();
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            smallCX = getMeasuredWidth() / 2;
            smallCY = getMeasuredHeight() / 2;
            invalidate();
            handler.removeMessages(0);
        }

        return true;
    }

    private void triggerMoveListener() {
        if (onDirectionListener != null) {
            onDirectionListener.onDirection(smallCX - bigCX, -1 * (smallCY - bigCY));
        }
    }
}
