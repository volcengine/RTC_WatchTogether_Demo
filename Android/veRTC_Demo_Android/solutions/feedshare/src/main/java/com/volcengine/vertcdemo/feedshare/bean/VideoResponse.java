package com.volcengine.vertcdemo.feedshare.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.util.List;

public class VideoResponse implements RTSBizResponse {
    @SerializedName("content_list")
    public List<VideoItem> videoItemList;
}
