package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;

import java.util.List;

public class ContentUpdateInform implements RTMBizInform {
    @SerializedName("content_list")
    public List<VideoItem> contentList;
}
