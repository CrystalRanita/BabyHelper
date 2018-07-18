package com.ranita.babyhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/*
* From https://stackoverflow.com/questions/18765445/select-a-portion-of-image-in-imageview-and-retrieve-the-end-points-of-selected-r
* Author: sandrstar, edited by Marek Sebera.
* I modify for multiple select for R G B area.
* */
public class DragRectView extends View{
    private Paint mRectPaint;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private boolean mDrawRect = false;

    private OnUpCallback mCallback = null;

    private Context mContext = null;

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public DragRectView(final Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DragRectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DragRectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     */
    public void setOnUpCallback(OnUpCallback callback) {
        mCallback = callback;
    }

    /**
     * Inits internal data
     */
    private void init() {
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.YELLOW);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(5); // TODO: should take from resources
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        // TODO: be aware of multi-touches
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawRect = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawRect || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }

                mDrawRect = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onRectFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mEndY, mStartX)));
                }
                invalidate();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawRect) {
            if(!Constants.getBoolSharedPref(Constants.RGB_SET_ALL, mContext)) {
                canvas.drawRect(
                        Math.min(mStartX, mEndX),
                        Math.min(mStartY, mEndY),
                        Math.max(mEndX, mStartX),
                        Math.max(mEndY, mStartY),
                        mRectPaint);
            }
        }

        if (Constants.getPosition(mContext, "G").left() != -1) {
            Paint paintG = new Paint();
            paintG.setStyle(Paint.Style.STROKE);
            paintG.setStrokeWidth(5);
            paintG.setColor(Color.GREEN);
            canvas.drawRect(
                    Constants.getPosition(mContext, "G").left(),
                    Constants.getPosition(mContext, "G").top(),
                    Constants.getPosition(mContext, "G").right(),
                    Constants.getPosition(mContext, "G").bottom(),
                    paintG
            );
        }
        if (Constants.getPosition(mContext, "R").left() != -1) {
            Paint paintR = new Paint();
            paintR.setStyle(Paint.Style.STROKE);
            paintR.setStrokeWidth(5);
            paintR.setColor(Color.RED);
            canvas.drawRect(
                    Constants.getPosition(mContext, "R").left(),
                    Constants.getPosition(mContext, "R").top(),
                    Constants.getPosition(mContext, "R").right(),
                    Constants.getPosition(mContext, "R").bottom(),
                    paintR
            );
        }
        if (Constants.getPosition(mContext, "B").left() != -1) {
            Paint paintB = new Paint();
            paintB.setStyle(Paint.Style.STROKE);
            paintB.setStrokeWidth(5);
            paintB.setColor(Color.BLUE);
            canvas.drawRect(
                    Constants.getPosition(mContext, "B").left(),
                    Constants.getPosition(mContext, "B").top(),
                    Constants.getPosition(mContext, "B").right(),
                    Constants.getPosition(mContext, "B").bottom(),
                    paintB
            );
        }
    }
}
