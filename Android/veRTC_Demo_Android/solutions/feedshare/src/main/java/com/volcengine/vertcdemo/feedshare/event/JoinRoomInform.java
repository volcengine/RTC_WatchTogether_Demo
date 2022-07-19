package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class JoinRoomInform implements RTMBizInform {
    @SerializedName("room_id")
    public String roomId;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
}
