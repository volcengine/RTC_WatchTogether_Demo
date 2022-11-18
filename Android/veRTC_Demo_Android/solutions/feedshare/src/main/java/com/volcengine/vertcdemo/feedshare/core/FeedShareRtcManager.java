package com.volcengine.vertcdemo.feedshare.core;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.LocalStreamStats;
import com.ss.bytertc.engine.type.MessageConfig;
import com.ss.bytertc.engine.type.NetworkQuality;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rts.RTCRoomEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTCVideoEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.feedshare.bean.MessageContent;
import com.volcengine.vertcdemo.feedshare.bean.SyncMessage;
import com.volcengine.vertcdemo.feedshare.event.RTCErrorEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCNetStatusEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserJoinEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserLeaveEvent;
import com.volcengine.vertcdemo.feedshare.feature.feedshare.ISyncHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FeedShareRTCManager {
    private static final String TAG = "FeedShareRtcManager";
    private final List<ISyncHandler> mSyncHandlers = new ArrayList<>(2);
    private RTCVideo mRTCVideo;
    private RTCRoom mRTCRoom;
    private FeedShareRTSClient mRTSClient;

    private final RTCVideoEventHandlerWithRTS mRTCVideoEventHandler = new RTCVideoEventHandlerWithRTS() {

        /**
         * 警告回调，详细可以看 {https://www.volcengine.com/docs/6348/70082#warncode}
         */
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            Log.d(TAG, "onWarning: " + warn);
        }

        /**
         * 错误回调，详细可以看 {https://www.volcengine.com/docs/6348/70082#errorcode}
         */
        @Override
        public void onError(int err) {
            super.onError(err);
            Log.d(TAG, "onError: " + err);
            SolutionDemoEventManager.post(new RTCErrorEvent(err));
        }

        @Override
        protected void onRTCMessageReceived(String fromUid, String message) {
            onMessageReceived(fromUid, message);
        }
    };

    private final RTCRoomEventHandlerWithRTS mRTCRoomEventHandler = new RTCRoomEventHandlerWithRTS() {

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            if (!isFirstJoinRoomSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new RTCErrorEvent(state));
            }
        }

        /**
         * 远端用户加入房间回调。
         */
        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            Log.d(TAG, "onUserJoined: " + userInfo.getUid());
            SolutionDemoEventManager.post(new RTCUserJoinEvent(userInfo.getUid()));
        }

        /**
         * 远端用户离开房间回调。
         */
        @Override
        public void onUserLeave(String uid, int reason) {
            super.onUserLeave(uid, reason);
            Log.d(TAG, "onUserLeave: " + uid);
            SolutionDemoEventManager.post(new RTCUserLeaveEvent(uid));
        }

        @Override
        public void onLocalStreamStats(LocalStreamStats stats) {
            boolean unblocked = stats.txQuality == NetworkQuality.NETWORK_QUALITY_EXCELLENT
                    || stats.txQuality == NetworkQuality.NETWORK_QUALITY_GOOD;
            SolutionDemoEventManager.post(new RTCNetStatusEvent(unblocked));
        }

        @Override
        protected void onRTCMessageReceived(String fromUid, String message) {
            super.onRTCMessageReceived(fromUid, message);
            onMessageReceived(fromUid, message);
        }
    };

    @Nullable
    public RTCVideo getEngine() {
        return mRTCVideo;
    }

    private FeedShareRTCManager() {
    }

    private static class Inner {
        private static final FeedShareRTCManager sInstance = new FeedShareRTCManager();
    }

    public static FeedShareRTCManager getInstance() {
        return Inner.sInstance;
    }

    public void destroyEngine() {
        if (mRTCRoom != null) {
            mRTCRoom.destroy();
        }
        if (mRTCVideo != null) {
            RTCVideo.destroyRTCVideo();
            mRTCVideo = null;
        }
    }

    public void initEngine(@NonNull RTSInfo info) {
        destroyEngine();
        mRTCVideo = RTCVideo.createRTCVideo(Utilities.getApplicationContext(), info.appId, mRTCVideoEventHandler, null, null);
        mRTCVideo.setBusinessId(info.appId);

        mRTSClient = new FeedShareRTSClient(mRTCVideo, info);
        mRTCVideoEventHandler.setBaseClient(mRTSClient);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);

        initVideoEffect();
    }

    public FeedShareRTSClient getRTMClient() {
        if (mRTSClient == null) {
            Log.i(TAG, "getRTMClient failed: mRTMClient is null");
        }
        return mRTSClient;
    }

    public void addSyncHandler(ISyncHandler syncHandler) {
        mSyncHandlers.add(syncHandler);
    }

    public void removeSyncHandler(ISyncHandler syncHandler) {
        mSyncHandlers.remove(syncHandler);
    }

    private static final int SEND_MESSAGE_ERROR_CODE_ENGINE_EMPTY = -1;

    /**
     * 给房间内的所有其他用户群发文本消息
     *
     * @param message 发送的文本消息内容消息不超过 62KB
     * @return >0：发送成功，返回这次发送消息的编号，从 1 开始递增; -1：发送失败
     */
    public long sendRoomMessage(String message) {
        if (mRTCVideo == null) {
            return SEND_MESSAGE_ERROR_CODE_ENGINE_EMPTY;
        }
        long sendResult = mRTCRoom.sendRoomMessage(message);
        Log.e(TAG, "sendRoomMessage message:" + message + ",sendResult:" + sendResult);
        return sendResult;
    }

    public long sendMessageToUser(String uid, String message) {
        if (mRTCVideo == null) {
            return SEND_MESSAGE_ERROR_CODE_ENGINE_EMPTY;
        }
        long sendResult = mRTCRoom.sendUserMessage(uid, message, MessageConfig.MessageConfigReliableOrdered);
        Log.e(TAG, "sendMessageToUser uid:" + uid + ",message:" + message + ",sendResult:" + sendResult);
        return sendResult;
    }

    public void onMessageReceived(String uid, String message) {
        try {
            JSONObject messageJson = new JSONObject(message);
            int messageType = messageJson.optInt("message_type");
            JSONObject content = messageJson.optJSONObject("content");
            for (ISyncHandler handler : mSyncHandlers) {
                if (handler == null) return;
                switch (messageType) {
                    case SyncMessage.FEED_SHARE_MESSAGE_TYPE_VIDEO_STATUS:
                        MessageContent videoStatus = MessageContent.fromJson(content);
                        handler.handleVideoStatus(uid, videoStatus);
                        break;
                    case SyncMessage.FEED_SHARE_MESSAGE_TYPE_REQUEST_FEED_SHARE:
                        handler.handleRequestFeedShare(uid);
                        break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "onMessageReceived parse message failed uid:" + uid + ",message:" + message);
        }
    }

    public void joinRoom(String roomId, String token, String userId) {
        Log.d(TAG, String.format("joinRoom: %s %s %s", roomId, userId, token));
        leaveRoom();
        if (mRTCVideo == null) {
            return;
        }
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mRTCRoomEventHandler);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
        UserInfo userInfo = new UserInfo(userId, null);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, true);
        mRTCRoom.joinRoom(token, userInfo, roomConfig);
    }

    public void leaveRoom() {
        Log.d(TAG, "leaveRoom");
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
    }

    /**
     * 初始化美颜
     */
    private void initVideoEffect() {

    }

    /**
     * 打开美颜对话框
     * @param context 上下文对象
     */
    public void openEffectDialog(Context context) {
        SafeToast.show("开源代码暂不支持美颜相关功能，体验效果请下载Demo");
    }
}
