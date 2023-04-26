// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.bean;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class JoinRoomResponse implements RTSBizResponse {

    public static final int FEED_SHARE_ROOM_SCENE_CHAT = 1;
    public static final int FEED_SHARE_ROOM_SCENE_FEED_SHARE = 2;

    @IntDef({FEED_SHARE_ROOM_SCENE_CHAT, FEED_SHARE_ROOM_SCENE_FEED_SHARE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ROOM_SCENE {
    }

    @SerializedName("room_id")
    public String roomId;
    @SerializedName("host_user_id")
    public String hostUid;
    @SerializedName("host_user_name")
    public String hostUname;
    @SerializedName("room_scene")
    @ROOM_SCENE
    public int roomScene;// 1. 连麦；2. 一起看
    @SerializedName("rtc_token")
    public String rtcToken;
    @SerializedName("content_list")
    public List<VideoItem> contentList;

}
