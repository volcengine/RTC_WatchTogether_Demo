// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.utils;

import android.view.View;
import android.widget.ImageView;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCVideo;
import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.bean.DeviceStatusRecord;

public class MicCameraSwitchHelper {
    private RTCVideo mEngine;
    private ImageView mMicBtn;
    private ImageView mCameraBtn;
    private View mStatusView;

    private final DeviceStatusRecord device;

    public MicCameraSwitchHelper() {
        this.device = DeviceStatusRecord.device;
    }

    public void setEngine(RTCVideo mEngine) {
        this.mEngine = mEngine;
    }

    public void setMicBtn(ImageView mMicBtn) {
        this.mMicBtn = mMicBtn;
    }

    public void setCameraBtn(ImageView mCameraBtn) {
        this.mCameraBtn = mCameraBtn;
    }

    public void setStatusView(View statusView) {
        this.mStatusView = statusView;
    }

    public void updateRtcAudioAndVideoCapture() {
        updateRtcAudioCapture();
        updateVideoRtcCapture();
    }

    public void updateRtcAudioCapture() {
        if (mEngine == null) {
            return;
        }
        if (device.isMicOn()) {
            mEngine.startAudioCapture();
        } else {
            mEngine.stopAudioCapture();
        }
    }

    public void updateVideoRtcCapture() {
        if (mEngine == null) {
            return;
        }
        if (device.isCameraOn()) {
            mEngine.startVideoCapture();
        } else {
            mEngine.stopVideoCapture();
        }
    }

    public void updateMicAndCameraUI() {
        updateMicUI();
        updateCameraUi();
    }

    public void updateMicUI() {
        if (mMicBtn != null) {
            mMicBtn.setImageResource(device.isMicOn() ? R.drawable.mic_on : R.drawable.mic_off_red);
        }
    }

    public void updateCameraUi() {
        if (mCameraBtn != null) {
            mCameraBtn.setImageResource(device.isCameraOn() ? R.drawable.camera_on : R.drawable.camera_off_red);
        }
        if (mStatusView != null) {
            mStatusView.setVisibility(device.isCameraOn() ? View.GONE : View.VISIBLE);
        }
    }

    public void openMic() {
        device.setMicOn();
        updateMicUI();
        updateRtcAudioCapture();
    }

    public void closeMic() {
        device.setMicOff();
        updateMicUI();
        updateRtcAudioCapture();
    }

    public void toggleMic() {
        device.toggleMic();
        updateMicUI();
        updateRtcAudioCapture();
    }

    public void openCamera() {
        device.setCameraOn();
        updateCameraUi();
        updateVideoRtcCapture();
    }

    public void closeCamera() {
        device.setCameraOff();
        updateCameraUi();
        updateVideoRtcCapture();
    }

    public void toggleCamera() {
        device.toggleCamera();
        updateCameraUi();
        updateVideoRtcCapture();
    }
}
