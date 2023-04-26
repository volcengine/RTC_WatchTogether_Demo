// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.feature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.feedshare.R;

public class AudioControllerDialog extends BaseDialog {

    public interface AudioChangeListener {
        void onVideoAudioChange(int progress);

        void onVideoStopTracking(int progress);

        void onRtcAudioChange(int progress);

        void onRtcStopTracking(int progress);

    }

    private AudioChangeListener mListener;
    private int mVideoDefault;
    private int mRtcDefault;
    private static final int VIDEO_MAX_VALUE = 200;
    private static final int RTC_MAX_VALUE = 200;

    public void setVideoDefault(int mVideoDefault) {
        this.mVideoDefault = mVideoDefault;
    }

    public void setRtcDefault(int mRtcDefault) {
        this.mRtcDefault = mRtcDefault;
    }

    public void setAudioChangeListener(AudioChangeListener li) {
        mListener = li;
    }

    public AudioControllerDialog(@NonNull Context context) {
        super(context);
    }

    public AudioControllerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AudioControllerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_audio_controller);
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setDimAmount(0);
        initUI();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        TextView videoAudioTv = findViewById(R.id.video_audio_tv);
        videoAudioTv.setText(mVideoDefault+"%");
        SeekBar videoAudioSb = findViewById(R.id.video_audio_sb);
        videoAudioSb.setMax(VIDEO_MAX_VALUE);
        videoAudioSb.setProgress(mVideoDefault);
        videoAudioSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mListener != null) {
                    mListener.onVideoAudioChange(progress);
                    videoAudioTv.setText(progress+"%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onVideoStopTracking(seekBar.getProgress());
                }
            }
        });
        TextView rtcAudioTv = findViewById(R.id.rtc_audio_tv);
        rtcAudioTv.setText(mRtcDefault+"%");
        SeekBar rtcAudioSb = findViewById(R.id.rtc_audio_sb);
        rtcAudioSb.setMax(RTC_MAX_VALUE);
        rtcAudioSb.setProgress(mRtcDefault);
        rtcAudioSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mListener != null) {
                    mListener.onRtcAudioChange(progress);
                    rtcAudioTv.setText(progress+"%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onRtcStopTracking(seekBar.getProgress());
                }
            }
        });
    }
}
