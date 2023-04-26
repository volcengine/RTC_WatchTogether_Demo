// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.bean;

import com.google.gson.annotations.SerializedName;

public class SyncMessage<T> {

    /***主播向观众同步视频播放操作和进度****/
    public static final int FEED_SHARE_MESSAGE_TYPE_VIDEO_STATUS = 1;
    /***观众请求开启一起看****/
    public static final int FEED_SHARE_MESSAGE_TYPE_REQUEST_FEED_SHARE = 2;

    //消息类型
    @SerializedName("message_type")
    public int messageType;

    //消息id, 用UUID生成，便于追踪消息传输路径
    @SerializedName("message_id")
    public String messageId;

    @SerializedName("content")
    //消息携带负载
    public T content;
}
