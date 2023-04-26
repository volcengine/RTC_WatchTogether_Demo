// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view;

import android.view.Surface;

public interface VideoController {

    int getDuration();

    void setSurface(Surface surface);

    void pause();

    void play();

    void release();

    void mute();

    void seekTo(int msec);

    boolean isPlaying();

    boolean isPaused();

    boolean isLooping();

    String getCover();

    int getVideoWidth();

    int getVideoHeight();

    int getCurrentPlaybackTime();

    void notifyProgressUpdate(int progress);
}
