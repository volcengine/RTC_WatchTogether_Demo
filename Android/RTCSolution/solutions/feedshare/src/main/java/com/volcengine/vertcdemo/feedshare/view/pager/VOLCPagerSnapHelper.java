// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.pager;

import android.util.Log;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class VOLCPagerSnapHelper extends PagerSnapHelper {
    public static final String TAG = "VOLCPagerSnapHelper";

    private PagerSelectListener mPagerSelectListener;

    @Override
    public int findTargetSnapPosition(final RecyclerView.LayoutManager layoutManager,
            final int velocityX, final int velocityY) {

        int targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX,
                velocityY);
        Log.d(TAG, "findTargetSnapPosition " + targetSnapPosition);
        if (mPagerSelectListener != null) {
            mPagerSelectListener.onPageSelected(targetSnapPosition, null);
        }
        return targetSnapPosition;
    }

    public void setCallback(final PagerSelectListener pagerSelectListener) {
        mPagerSelectListener = pagerSelectListener;
    }
}
