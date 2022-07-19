package com.volcengine.vertcdemo.feedshare.feature.feedshare;

import android.util.Log;

import com.volcengine.vertcdemo.feedshare.bean.VideoStatusInfo;
import com.volcengine.vertcdemo.feedshare.bean.SyncMessage;

import org.json.JSONObject;

import java.util.UUID;

public class SyncMessageUtil {
    private static final String TAG = "SyncMessageUtil";

    public static String createVideoStatusMessage(VideoStatusInfo statusInfo) {
        JSONObject msg = createMsgJson(SyncMessage.FEED_SHARE_MESSAGE_TYPE_VIDEO_STATUS);
        addVideoStatusInfo(statusInfo, msg);
        return msg.toString();
    }

    public static String createRequestShareFeedMessage() {
        JSONObject msg = createMsgJson(SyncMessage.FEED_SHARE_MESSAGE_TYPE_REQUEST_FEED_SHARE);
        return msg.toString();
    }

    private static JSONObject createMsgJson(int msgType) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("message_type", msgType);
            msg.put("message_id", getMessageId());
            JSONObject content = new JSONObject();
            msg.put("content", content);
        } catch (Exception e) {
            Log.e(TAG, "createMsgJson fail:" + e);
        }
        return msg;
    }

    private static JSONObject addVideoStatusInfo(VideoStatusInfo progress, JSONObject msg) {
        if (progress == null || msg == null) {
            return msg;
        }
        JSONObject content = msg.optJSONObject("content");
        if (content == null) {
            return msg;
        }
        try {
            content.put("video_status", VideoStatusInfo.toJson(progress));
            msg.put("content", content);
        } catch (Exception e) {
            //ignore
        }
        return msg;
    }

    private static String getMessageId() {
        return UUID.randomUUID().toString();
    }
}
