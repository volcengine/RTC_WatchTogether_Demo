// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.utils.Utils;

import java.util.List;

public class SolutionSeekBar extends View {

    private float mProgress;
    private float mSecondaryProgress;
    private int mProgressHeight;

    private int mProgressColor;
    private int mSecondaryProgressColor;
    private int mBackgroundProgressColor;
    private int mThumbColor;
    private float mThumbRadius;
    private float mThumbRadiusOnDragging;
    private final float mRedThumbRadiusOnDragging;
    private final boolean mIsRoundEndStyle;

    private float mThumbPosition;
    protected float mProgressLength;
    private boolean isThumbOnDragging;

    private float mLeft;
    private float mRight;
    private final Paint mPaint;
    public boolean mIsFullScreen;
    public boolean mHideMarks;
    private float dx;

    private List<Mark> mMarkList;
    private OnByteSeekBarChangeListener mOnByteSeekBarChangeListener;
    private VelocityTracker mVelocityTracker;

    public SolutionSeekBar(Context context) {
        this(context, null);
    }

    public SolutionSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SolutionSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SolutionSeekBar, defStyleAttr,
                0);
        mThumbColor = a.getColor(R.styleable.SolutionSeekBar_thumb_color,
                ContextCompat.getColor(context, R.color.white));
        mThumbRadius = a.getDimensionPixelSize(R.styleable.SolutionSeekBar_thumb_radius, 15);
        mThumbRadiusOnDragging = a.getDimensionPixelSize(
                R.styleable.SolutionSeekBar_thumb_radius_on_dragging, 20);
        mRedThumbRadiusOnDragging = a.getDimensionPixelSize(
                R.styleable.SolutionSeekBar_thumb_radius_on_dragging, 26);
        mProgressHeight = a.getDimensionPixelSize(R.styleable.SolutionSeekBar_progress_height,
                (int) Utils.dp2Px(2));
        mProgressColor = a.getColor(R.styleable.SolutionSeekBar_track_color,
                ContextCompat.getColor(context, R.color.red_100));
        mSecondaryProgressColor = a.getColor(R.styleable.SolutionSeekBar_secondary_progress_color,
                ContextCompat.getColor(context, R.color.white_70));
        mBackgroundProgressColor = a.getColor(R.styleable.SolutionSeekBar_background_progress_color,
                ContextCompat.getColor(context, R.color.white_38));
        mIsRoundEndStyle = a.getBoolean(R.styleable.SolutionSeekBar_round_point_style, false);
        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int suggestHeight = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int height = (int) mThumbRadiusOnDragging * 2 + getPaddingTop() + getPaddingBottom();
        if (suggestHeight < height) {
            suggestHeight = height;
        }
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                suggestHeight);

        mLeft = getPaddingLeft() + mRedThumbRadiusOnDragging;
        mRight = getMeasuredWidth() - getPaddingRight() - mRedThumbRadiusOnDragging;
        mProgressLength = mRight - mLeft;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float progressY = getPaddingTop() / 2.0f + getMeasuredHeight() / 2.0f;
        float strokeSize = mProgressHeight;
        float fixedStrokeSize = strokeSize - 1f;
        if (mProgress != 0) {
            mThumbPosition = mProgressLength / 100 * mProgress + mLeft;
        } else {
            mThumbPosition = mLeft;
        }
        float secondaryProgressPosition;
        if (mSecondaryProgress != 0) {
            secondaryProgressPosition = mProgressLength / 100 * mSecondaryProgress + mLeft;
        } else {
            secondaryProgressPosition = mLeft;
        }

        // draw background progress
        mPaint.setStrokeWidth(fixedStrokeSize);
        mPaint.setColor(mBackgroundProgressColor);
        canvas.drawLine(mLeft, progressY, mRight, progressY, mPaint);
        if (mIsRoundEndStyle) {
            drawSemiCircle(canvas, mLeft, mRight, progressY, fixedStrokeSize);
        }

        // draw second progress
        mPaint.setStrokeWidth(fixedStrokeSize);
        mPaint.setColor(mSecondaryProgressColor);
        canvas.drawLine(mLeft, progressY, secondaryProgressPosition, progressY, mPaint);
        if (mIsRoundEndStyle) {
            drawSemiCircle(canvas, mLeft, secondaryProgressPosition, progressY, fixedStrokeSize);
        }

        // draw progress
        mPaint.setStrokeWidth(strokeSize);
        mPaint.setColor(mProgressColor);
        canvas.drawLine(mLeft, progressY, mThumbPosition, progressY, mPaint);
        if (mIsRoundEndStyle) {
            drawSemiCircle(canvas, mLeft, mThumbPosition, progressY, strokeSize);
        }

        drawCustomMarks(canvas);

        if (isThumbOnDragging) {
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.red_38));
            mPaint.setStrokeWidth(mRedThumbRadiusOnDragging);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mThumbPosition, progressY, mRedThumbRadiusOnDragging, mPaint);
        }

        // draw thumb
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mThumbColor);
        mPaint.setStrokeWidth(strokeSize);
        canvas.drawCircle(mThumbPosition, progressY, mThumbRadius, mPaint);
    }

    private void drawSemiCircle(Canvas canvas, float startX, float endX, float y,
            float strokeWidth) {
        float radius = strokeWidth / 2;
        mPaint.setStrokeWidth(0);
        RectF startRect = new RectF(startX - radius, y - radius, startX + radius, y + radius);
        canvas.drawArc(startRect, 90, 180, true, mPaint);
        RectF endRect = new RectF(endX - radius, y - radius, endX + radius, y + radius);
        canvas.drawArc(endRect, -90, 180, true, mPaint);
        mPaint.setStrokeWidth(strokeWidth);
    }

    private void drawCustomMarks(Canvas canvas) {
        if (mMarkList == null || mMarkList.isEmpty() || mHideMarks) {
            return;
        }
        float progressY = getPaddingTop() / 2.0f + getMeasuredHeight() / 2.0f;
        for (Mark mark : mMarkList) {
            if (mark != null) {
                mPaint.setColor(ContextCompat.getColor(getContext(),
                        mark.alreadyPass ? R.color.white : mark.color));
                if (mark.totalLength != 0 && mProgressLength != 0) {
                    float markLeft = ((float) mark.markStartPoint / (float) mark.totalLength)
                                     * mProgressLength + getPaddingLeft();
                    if (markLeft < mLeft) {
                        markLeft = mLeft;
                    }
                    float markRight = markLeft + Utils.dp2Px(mIsFullScreen ? 4f : 2f);
                    if (markRight > mRight) {
                        markRight = mRight;
                    }
                    canvas.drawLine(markLeft, progressY, markRight, progressY, mPaint);
                    if (mIsRoundEndStyle) {
                        drawSemiCircle(canvas, markLeft, markRight, progressY, mProgressHeight);
                    }
                }
            }
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isThumbOnDragging = isThumbTouched(event);
                if (isThumbOnDragging) {
                    if (mOnByteSeekBarChangeListener != null) {
                        mOnByteSeekBarChangeListener.onStartTrackingTouch(SolutionSeekBar.this);
                    }
                    invalidate();
                } else if (isTrackTouched(event)) {
                    if (mOnByteSeekBarChangeListener != null) {
                        mOnByteSeekBarChangeListener.onStartTrackingTouch(SolutionSeekBar.this);
                    }
                    mThumbPosition = event.getX();
                    if (mThumbPosition < mLeft) {
                        mThumbPosition = mLeft;
                    }
                    if (mThumbPosition > mRight) {
                        mThumbPosition = mRight;
                    }
                    if (mProgressLength != 0) {
                        mProgress = (int) ((mThumbPosition - mLeft) * 100 / mProgressLength);
                    }
                    if (mOnByteSeekBarChangeListener != null) {
                        mOnByteSeekBarChangeListener.onProgressChanged(SolutionSeekBar.this, mProgress,
                                true, 0);
                    }
                    invalidate();
                    isThumbOnDragging = true;
                }
                dx = mThumbPosition - event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isThumbOnDragging) {
                    mThumbPosition = event.getX() + dx;
                    if (mThumbPosition < mLeft) {
                        mThumbPosition = mLeft;
                    }
                    if (mThumbPosition > mRight) {
                        mThumbPosition = mRight;
                    }
                    if (mProgressLength != 0) {
                        mProgress = (int) ((mThumbPosition - mLeft) * 100 / mProgressLength);
                    }
                    invalidate();
                    if (mOnByteSeekBarChangeListener != null) {
                        float xVelocity = 0f;
                        if (mVelocityTracker != null) {
                            mVelocityTracker.computeCurrentVelocity(1000);
                            xVelocity = Math.abs(mVelocityTracker.getXVelocity());
                        }
                        mOnByteSeekBarChangeListener.onProgressChanged(SolutionSeekBar.this, mProgress,
                                true, xVelocity);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    if (mOnByteSeekBarChangeListener != null) {
                        mOnByteSeekBarChangeListener.onStartTrackingTouch(SolutionSeekBar.this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                isThumbOnDragging = false;
                if (mOnByteSeekBarChangeListener != null) {
                    mOnByteSeekBarChangeListener.onStopTrackingTouch(SolutionSeekBar.this);
                }
                getParent().requestDisallowInterceptTouchEvent(false);

                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mThumbPosition = event.getX() + dx;
                if (mThumbPosition < mLeft) {
                    mThumbPosition = mLeft;
                }
                if (mThumbPosition > mRight) {
                    mThumbPosition = mRight;
                }
                if (mProgressLength != 0) {
                    mProgress = (int) ((mThumbPosition - mLeft) * 100 / mProgressLength);
                }
                if (mOnByteSeekBarChangeListener != null) {
                    if (isThumbOnDragging) {
                        mOnByteSeekBarChangeListener.onStopTrackingTouch(SolutionSeekBar.this);
                    }
                }
                isThumbOnDragging = false;
                invalidate();
                break;
            default:
                break;
        }
        return isThumbOnDragging || super.onTouchEvent(event);
    }

    private boolean isThumbTouched(MotionEvent event) {
        if (!isEnabled() || mProgress == 0) {
            return false;
        }
        float x = mProgressLength / 100 * mProgress + mLeft;
        float y = getMeasuredHeight() / 2.0f;
        return Math.pow(event.getX() - x, 2) + Math.pow(event.getY() - y, 2) <= Math.pow(
                getMeasuredHeight() / 2.0f, 2);
    }

    private boolean isTrackTouched(MotionEvent event) {
        float xPoint = event.getX();
        float yPoint = event.getY();
        return isEnabled()
               && xPoint >= getPaddingLeft()
               && xPoint <= getMeasuredWidth() - getPaddingRight()
               && yPoint >= 0
               && yPoint <= getMeasuredHeight();
    }

    public void setProgressHeight(int height) {
        mProgressHeight = height;
        invalidate();
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        invalidate();
    }

    public void setSecondaryProgressColor(int secondaryProgressColor) {
        mSecondaryProgressColor = secondaryProgressColor;
        invalidate();
    }

    public void setBackgroundProgressColor(int backgroundProgressColor) {
        mBackgroundProgressColor = backgroundProgressColor;
        invalidate();
    }

    public void setThumbColor(int thumbColor) {
        mThumbColor = thumbColor;
        invalidate();
    }

    public void setThumbRadius(float thumbRadius) {
        mThumbRadius = thumbRadius;
        invalidate();
    }

    public void setThumbRadiusOnDragging(float thumbRadiusOnDragging) {
        mThumbRadiusOnDragging = thumbRadiusOnDragging;
        requestLayout();
    }

    public boolean isHideMarks() {
        return mHideMarks;
    }

    public void setHideMarks(boolean hideMarks) {
        mHideMarks = hideMarks;
        invalidate();
    }

    public void setProgress(float progress) {
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        if (mOnByteSeekBarChangeListener != null) {
            mOnByteSeekBarChangeListener.onProgressChanged(this, progress, false, 0);
        }
        invalidate();
    }

    public int getProgress() {
        return Math.round(mProgress);
    }

    public int getSecondaryProgress() {
        return Math.round(mSecondaryProgress);
    }

    public void setSecondaryProgress(float secondaryProgress) {
        mSecondaryProgress = secondaryProgress;
        invalidate();
    }

    public void setMarkList(List<Mark> markList) {
        mMarkList = markList;
        invalidate();
    }

    public List<Mark> getMarkList() {
        return mMarkList;
    }

    public void setOnByteSeekBarChangeListener(
            OnByteSeekBarChangeListener onByteSeekBarChangeListener) {
        mOnByteSeekBarChangeListener = onByteSeekBarChangeListener;
    }

    public interface OnByteSeekBarChangeListener {

        void onProgressChanged(SolutionSeekBar seekBar, float progress, boolean fromUser,
                               float xVelocity);

        void onStartTrackingTouch(SolutionSeekBar seekBar);

        void onStopTrackingTouch(SolutionSeekBar seekBar);
    }

    public static class Mark {
        long totalLength;
        long markStartPoint;
        int color;
        String commodityId;
        boolean alreadyPass = false;

        public Mark(long totalLength, String commodityId, long markStartPoint,
                @ColorRes int color) {
            this.commodityId = commodityId;
            this.totalLength = totalLength;
            this.markStartPoint = markStartPoint;
            this.color = color;
        }
    }
}
