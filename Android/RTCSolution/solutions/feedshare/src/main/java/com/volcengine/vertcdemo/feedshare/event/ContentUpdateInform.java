// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;

import java.util.List;

public class ContentUpdateInform implements RTSBizInform {
    @SerializedName("content_list")
    public List<VideoItem> contentList;
}
