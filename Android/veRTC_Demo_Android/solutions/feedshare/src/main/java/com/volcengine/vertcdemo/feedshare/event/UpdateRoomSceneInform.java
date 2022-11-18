package com.volcengine.vertcdemo.feedshare.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;

public class UpdateRoomSceneInform implements RTSBizInform {
    @SerializedName("room_id")
    public String roomId;
    @SerializedName("room_scene")
    public int roomScene;
}
