// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;

public class FinishRoomInform implements RTSBizInform {
    /**
     * 正常退出
     */
    public static final int TYPE_NORMAL = 1;
    /**
     * 超时退出
     */
    public static final int TYPE_TIMEOUT = 2;
    /**
     * 清理退出
     */
    public static final int TYPE_CLEAR = 3;

    @SerializedName("room_id")
    public String roomId;

    @SerializedName("type")
    public int type;
}
