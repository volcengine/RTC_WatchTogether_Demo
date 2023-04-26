// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.bean;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class MessageContent {
    public static final String KEY_VIDEO_STATUS = "video_status";

    @SerializedName(KEY_VIDEO_STATUS)
    public VideoStatusInfo videoStatus;

    public static MessageContent fromJson(JSONObject json ) {
        MessageContent content = null;
        try {
            content = new MessageContent();
            JSONObject progress = json.optJSONObject(KEY_VIDEO_STATUS);
            if (progress != null) {
                content.videoStatus = VideoStatusInfo.fromJson(progress);
            }
        } catch (Exception e) {
            Log.e("MessageContent", "fromJson failed:" + e);
        }
        return content;
    }
}
