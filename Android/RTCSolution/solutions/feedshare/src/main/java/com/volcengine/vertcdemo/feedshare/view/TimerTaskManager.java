// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view;

import android.os.Handler;
import android.os.Looper;

public class TimerTaskManager {
    private Handler mHandler;
    private TaskListener mTaskListener;
    private boolean mTracking = false;

    private final Runnable mTrackingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTaskListener == null) {
                stopTracking();
                return;
            }

            final long delay = mTaskListener.onTaskExecute();
            if (delay > 0) {
                mHandler.postDelayed(this, delay);
            } else {
                stopTracking();
            }
        }
    };

    public TimerTaskManager(final Looper looper, final TaskListener taskListener) {
        mHandler = new Handler(looper);
        mTaskListener = taskListener;
    }

    public void startTracking() {
        if (mTracking) {
            return;
        }

        mTracking = true;
        mHandler.removeCallbacks(mTrackingRunnable);
        mHandler.post(mTrackingRunnable);
    }

    public void stopTracking() {
        if (!mTracking) {
            return;
        }

        mTracking = false;
        mHandler.removeCallbacks(mTrackingRunnable);
    }

    public void release() {
        mTracking = false;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mTaskListener = null;
    }

    public interface TaskListener {
        long onTaskExecute();
    }
}
