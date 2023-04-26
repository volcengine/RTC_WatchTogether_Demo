// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.bean;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class VideoStatusInfo {
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_PLAYING = 2;

    @SerializedName("video_id")
    public String videoId;
    @SerializedName("progress")
    public int progress;
    @SerializedName("status")
    public int status;//1. 暂停中 2.播放中

    public static VideoStatusInfo fromJson(JSONObject json) {
        VideoStatusInfo info = null;
        try {
            info = new VideoStatusInfo();
            info.videoId = json.optString("video_id");
            info.progress = json.optInt("progress");
            info.status = json.optInt("status");
        } catch (Exception e) {
            Log.e("VideoStatusInfo", "fromJson failed:" + e);
        }
        return info;
    }

    public static JSONObject toJson(VideoStatusInfo info) {
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject();
            json.put("video_id", info.videoId);
            json.put("progress", info.progress);
            json.put("status", info.status);
        } catch (Exception e) {
            Log.e("VideoStatusInfo", "fromJson failed:" + e);
        }

        return json;
    }
}
