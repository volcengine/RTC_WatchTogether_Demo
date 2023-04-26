// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class DisplayMode {
    /**
     * fitXY
     */
    public static final int DISPLAY_MODE_DEFAULT = 0;

    /**
     * The screen width is full of controls, and the height is adapted to the video ratio
     */
    public static final int DISPLAY_MODE_ASPECT_FILL_X = 1;

    /**
     * The screen height is full of controls, and the width is adapted to the video ratio
     */
    public static final int DISPLAY_MODE_ASPECT_FILL_Y = 2;
    /**
     * centerInside
     */
    public static final int DISPLAY_MODE_ASPECT_FIT = 3;
    /**
     * centerCrop
     */
    public static final int DISPLAY_MODE_ASPECT_FILL = 4;

    private static final String TAG = "DisplayMode";

    private int videoWidth;
    private int videoHeight;
    private int displayMode = DISPLAY_MODE_DEFAULT;

    private FrameLayout containerView;
    private View displayView;

    public void setVideoSize(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        apply();
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        apply();
    }

    public int getDisplayMode() {
        return this.displayMode;
    }

    public void setContainerView(FrameLayout containerView) {
        this.containerView = containerView;
        apply();
    }

    public void setDisplayView(View displayView) {
        this.displayView = displayView;
        apply();
    }

    public void apply() {
        if (this.displayView == null) return;
        this.displayView.removeCallbacks(applyDisplayMode);
        this.displayView.postOnAnimation(applyDisplayMode);
    }

    private final Runnable applyDisplayMode = this::applyDisplayMode;

    private void applyDisplayMode() {
        final View containerView = this.containerView;
        if (containerView == null) {
            return;
        }

        final int containerWidth = containerView.getWidth();
        final int containerHeight = containerView.getHeight();

        final View displayView = this.displayView;
        if (displayView == null) {
            return;
        }

        final int displayMode = this.displayMode;
        final int videoWidth = this.videoWidth;
        final int videoHeight = this.videoHeight;
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }

        final float videoRatio = videoWidth / (float) videoHeight;
        final float containerRatio = containerWidth / (float) containerHeight;

        final int displayGravity = Gravity.CENTER;
        final int displayWidth;
        final int displayHeight;

        switch (displayMode) {
            case DISPLAY_MODE_DEFAULT:
                displayWidth = containerWidth;
                displayHeight = containerHeight;
                break;
            case DISPLAY_MODE_ASPECT_FILL_X:
                displayWidth = containerWidth;
                displayHeight = (int) (containerWidth / videoRatio);
                break;
            case DISPLAY_MODE_ASPECT_FILL_Y:
                displayWidth = (int) (containerHeight * videoRatio);
                displayHeight = containerHeight;
                break;
            case DISPLAY_MODE_ASPECT_FIT:
                if (videoRatio >= containerRatio) {
                    displayWidth = containerWidth;
                    displayHeight = (int) (containerWidth / videoRatio);
                } else {
                    displayWidth = (int) (containerHeight * videoRatio);
                    displayHeight = containerHeight;
                }
                break;
            case DISPLAY_MODE_ASPECT_FILL:
                if (videoRatio >= containerRatio) {
                    displayWidth = (int) (containerHeight * videoRatio);
                    displayHeight = containerHeight;
                } else {
                    displayWidth = containerWidth;
                    displayHeight = (int) (containerWidth / videoRatio);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown displayMode = " + displayMode);
        }

        final LayoutParams displayLP = (LayoutParams) displayView.getLayoutParams();
        if (displayLP == null) {
            return;
        }

        if (displayLP.height != displayHeight
            || displayLP.width != displayWidth
            || displayLP.gravity != displayGravity) {
            displayLP.gravity = displayGravity;
            displayLP.width = displayWidth;
            displayLP.height = displayHeight;
            displayView.requestLayout();
            Log.i(TAG, displayLP.width + "," + displayLP.height);
        }
    }
}
