package com.demo.opengles.world.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * 眼睛位置控制器
 */
public class JumpControlView extends View {

    private Paint paint;
    private int smallCX;
    private int smallCY;
    private float smallRadius;

    public OnJumpListener onJumpListener;

    public interface OnJumpListener {
        void onJump(float progress);
    }

    public void setOnJumpListener(OnJumpListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    public JumpControlView(Context context) {
        super(context);
        init();
    }

    public JumpControlView(Context context, @Nullable AttributeSet attrs) {
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

        smallRadius = getMeasuredWidth() / 2f - 5;

        smallCX = getMeasuredWidth() / 2;
        smallCY = (int) (getMeasuredHeight() - smallRadius - 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        canvas.drawCircle(smallCX, smallCY, smallRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            smallCY = (int) event.getY();
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            smallCY = (int) event.getY();
            invalidate();
        }

        smallCY = (int) Math.max(smallRadius + 5, smallCY);
        smallCY = (int) Math.min(getHeight() - smallRadius - 5, smallCY);

        float progress = (getHeight() - smallRadius - 5 - smallCY) / (getHeight() - smallRadius - 5 - smallRadius - 5);
        triggerMoveListener(progress);
        return true;
    }

    private void triggerMoveListener(float progress) {
        if (onJumpListener != null) {
            onJumpListener.onJump(progress);
        }
    }
}
