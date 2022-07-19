package com.volcengine.vertcdemo.feedshare.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;

import java.util.List;

public class VideoResponse implements RTMBizResponse {
    @SerializedName("content_list")
    public List<VideoItem> videoItemList;
}
