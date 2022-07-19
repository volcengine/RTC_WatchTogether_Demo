package com.volcengine.vertcdemo.feedshare.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rtm.RTCEventHandlerWithRTM;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.feedshare.event.RTCErrorEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCNetStatusEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserJoinEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserLeaveEvent;
import com.volcengine.vertcdemo.feedshare.feature.feedshare.ISyncHandler;
import com.volcengine.vertcdemo.feedshare.bean.MessageContent;
import com.volcengine.vertcdemo.feedshare.bean.SyncMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FeedShareRtcManager {
    private static final String TAG = "FeedShareRtcManager";
    private final List<ISyncHandler> mSyncHandlers = new ArrayList<>(2);
    private RTCEngine mEngine;
    private FeedShareRTMClient mRTMClient;
    private final RTCEventHandlerWithRTM mRtcEventHandler = new RTCEventHandlerWithRTM() {

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            if (!isFirstJoinRoomSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new RTCErrorEvent(state));
            }
        }

        /**
         * 第一帧远端视频流在视图上渲染成功后，收到此回调。
         */
        @Override
        public void onFirstRemoteVideoFrameRendered(RemoteStreamKey remoteStreamKey, VideoFrameInfo frameInfo) {
            super.onFirstRemoteVideoFrameRendered(remoteStreamKey, frameInfo);
            Log.d(TAG, "onFirstRemoteVideoFrame: " + remoteStreamKey.toString());
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
        public void onLocalStreamStats(LocalStreamStats stats) {
            boolean unblocked = stats.txQuality == NetworkQuality.NETWORK_QUALITY_EXCELLENT
                    || stats.txQuality == NetworkQuality.NETWORK_QUALITY_GOOD;
            SolutionDemoEventManager.post(new RTCNetStatusEvent(unblocked));
        }

        @Override
        protected void onRTCMessageReceived(String fromUid, String message) {
            onMessageReceived(fromUid, message);
        }
    };

    @Nullable
    public RTCEngine getEngine() {
        return mEngine;
    }

    private FeedShareRtcManager() {
    }

    private static class Inner {
        private static final FeedShareRtcManager sInstance = new FeedShareRtcManager();
    }

    public static FeedShareRtcManager getInstance() {
        return Inner.sInstance;
    }

    public void clear() {
        if (mEngine != null) {
            RTCEngine.destroyEngine(mEngine);
        }
        mEngine = null;
    }

    public void init(@NonNull RtmInfo info) {
        final RTCEngine engine = RTCEngine.createEngine(Utilities.getApplicationContext(), info.appId, mRtcEventHandler, null, null);
        if (engine == null) {
            throw new IllegalStateException("Failed to create RTCEngine.");
        }
        final FeedShareRTMClient client = new FeedShareRTMClient(engine, info);

        mEngine = engine;
        mEngine.setBusinessId(info.bid);
        mRTMClient = client;
        mRtcEventHandler.setBaseClient(mRTMClient);
    }

    public FeedShareRTMClient getRTMClient() {
        if (mRTMClient == null) {
            Log.i(TAG, "getRTMClient failed: mRTMClient is null");
        }
        return mRTMClient;
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
        if (mEngine == null) {
            return SEND_MESSAGE_ERROR_CODE_ENGINE_EMPTY;
        }
        long sendResult = mEngine.sendRoomMessage(message);
        Log.e(TAG, "sendRoomMessage message:" + message + ",sendResult:" + sendResult);
        return sendResult;
    }

    public long sendMessageToUser(String uid, String message) {
        if (mEngine == null) {
            return SEND_MESSAGE_ERROR_CODE_ENGINE_EMPTY;
        }
        long sendResult = mEngine.sendUserMessage(uid, message);
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

}
