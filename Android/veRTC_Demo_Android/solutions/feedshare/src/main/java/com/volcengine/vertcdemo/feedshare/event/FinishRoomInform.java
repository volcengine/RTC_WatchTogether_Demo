package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class FinishRoomInform implements RTMBizInform {
    @SerializedName("room_id")
    public String roomId;
}
