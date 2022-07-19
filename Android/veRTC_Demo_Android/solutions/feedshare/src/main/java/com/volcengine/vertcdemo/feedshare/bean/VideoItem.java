package com.volcengine.vertcdemo.feedshare.bean;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import org.json.JSONObject;

import kotlin.jvm.Transient;

public class VideoItem {
    @SerializedName("VideoId")
    public String videoId;
    @SerializedName("VideoTitle")
    public String title;
    @SerializedName("VideoDuration")
    public float duration;
    @SerializedName("VideoCoverUrl")
    public String coverUrl;
    @SerializedName("VideoUrl")
    public String videoUrl;
    /****当前视频对应的播放数据封装*/
    @Transient
    public StrategySource playStrategySource;

    public static VideoItem fromJson(JSONObject json) {
        VideoItem videoItem = null;
        try {
            videoItem = new VideoItem();
            videoItem.videoId = json.optString("VideoId");
            videoItem.title = json.optString("VideoTitle");
            Double d = json.optDouble("VideoDuration");
            videoItem.duration = Float.parseFloat(String.valueOf(d));
            videoItem.coverUrl = json.optString("VideoCoverUrl");
            videoItem.videoUrl = json.optString("VideoUrl");
        } catch (Exception e) {
            Log.e("VideoItem", "fromJson failed:" + e);
        }
        return videoItem;
    }

    public static JSONObject toJson(VideoItem item) {
        JSONObject json = new JSONObject();
        try {
            json.put("VideoId", item.videoId);
            json.put("VideoTitle", item.title);
            json.put("VideoDuration", item.duration);
            json.put("VideoCoverUrl", item.coverUrl);
            json.put("VideoUrl", item.videoUrl);
        } catch (Exception e) {
            Log.e("VideoItem", "fromJson failed:" + e);
        }
        return json;
    }
}
