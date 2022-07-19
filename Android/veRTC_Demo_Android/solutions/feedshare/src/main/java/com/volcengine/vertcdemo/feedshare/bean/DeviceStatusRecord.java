package com.volcengine.vertcdemo.feedshare.bean;

public class DeviceStatusRecord {
    public static final DeviceStatusRecord device = new DeviceStatusRecord();

    private boolean mCameraStatus = true;
    private boolean mMicStatus = true;

    private DeviceStatusRecord() {
    }

    public boolean isMicOn() {
        return mMicStatus;
    }

    public void setMicOn() {
        mMicStatus = true;
    }

    public void setMicOff() {
        mMicStatus = false;
    }

    public void toggleMic() {
        mMicStatus = !mMicStatus;
    }

    public boolean isCameraOn() {
        return mCameraStatus;
    }

    public void setCameraOn() {
        mCameraStatus = true;
    }

    public void setCameraOff() {
        mCameraStatus = false;
    }

    public void toggleCamera() {
        mCameraStatus = !mCameraStatus;
    }
}
