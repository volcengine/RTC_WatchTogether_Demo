package com.volcengine.vertcdemo.feedshare.core;

import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.ss.bytertc.base.utils.NetworkUtils;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.video.rtc.demo.basic_module.utils.AppExecutors;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.common.AbsBroadcast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.feedshare.BuildConfig;
import com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse;
import com.volcengine.vertcdemo.feedshare.bean.VideoResponse;
import com.volcengine.vertcdemo.feedshare.event.ContentUpdateInform;
import com.volcengine.vertcdemo.feedshare.event.FinishRoomInform;
import com.volcengine.vertcdemo.feedshare.event.JoinRoomInform;
import com.volcengine.vertcdemo.feedshare.event.LeaveRoomInform;
import com.volcengine.vertcdemo.feedshare.event.UpdateRoomSceneInform;

import java.util.UUID;

public class FeedShareRTSClient extends RTSBaseClient {
    private static final String CMD_JOIN_ROOM = "twJoinRoom";
    private static final String CMD_LEAVE_ROOM = "twLeaveRoom";
    private static final String CMD_UPDATE_ROOM_SCENE = "twUpdateRoomScene";
    private static final String CMD_GET_CONTENT_LIST = "twGetContentList";
    private static final String CLEAR_USER = "twClearUser";

    public FeedShareRTSClient(@NonNull RTCVideo rtcVideo, @NonNull RTSInfo rtmInfo) {
        super(rtcVideo, rtmInfo);
        initEventListener();
    }

    public void joinRoom(IRequestCallback<JoinRoomResponse> callback) {
        if (isNetworkDisabled()) {
            return;
        }
        AppExecutors.networkIO().execute(() -> {
            JsonObject params = getCommonParams(CMD_JOIN_ROOM);
            params.addProperty("user_name", SolutionDataManager.ins().getUserName());
            sendServerMessage(CMD_JOIN_ROOM, FeedShareDataManger.getInstance().getRoomId(), params, JoinRoomResponse.class, callback);
        });
    }

    public void leaveRoom(IRequestCallback callback) {
        if (isNetworkDisabled()) {
            return;
        }
        AppExecutors.networkIO().execute(() -> {
            JsonObject params = getCommonParams(CMD_LEAVE_ROOM);
            sendServerMessage(CMD_LEAVE_ROOM, FeedShareDataManger.getInstance().getRoomId(), params, null, callback);
        });
    }

    public void updateRoomScene(IRequestCallback callback, int targetScene) {
        if (isNetworkDisabled()) {
            return;
        }
        AppExecutors.networkIO().execute(() -> {
            JsonObject params = getCommonParams(CMD_UPDATE_ROOM_SCENE);
            params.addProperty("room_scene", targetScene);
            sendServerMessage(CMD_UPDATE_ROOM_SCENE, FeedShareDataManger.getInstance().getRoomId(), params, null, callback);
        });
    }

    public void getContentList(IRequestCallback<VideoResponse> callback) {
        if (isNetworkDisabled()) {
            return;
        }
        AppExecutors.networkIO().execute(() -> {
            JsonObject params = getCommonParams(CMD_GET_CONTENT_LIST);
            params.addProperty("dt", Build.DEVICE);
            params.addProperty("device_brand", Build.BRAND);
            params.addProperty("os", "Android");
            params.addProperty("os_version", Build.VERSION.RELEASE);
            params.addProperty("client_version", BuildConfig.APP_VERSION_NAME);
            sendServerMessage(CMD_GET_CONTENT_LIST, FeedShareDataManger.getInstance().getRoomId(), params, VideoResponse.class, callback);
        });
    }

    public void requestClearUser() {
        AppExecutors.networkIO().execute(() -> {
            JsonObject params = getCommonParams(CLEAR_USER);
            sendServerMessage(CLEAR_USER, FeedShareDataManger.getInstance().getRoomId(), params, null, null);
        });
    }

    private JsonObject getCommonParams(String cmd) {
        JsonObject params = new JsonObject();
        params.addProperty("app_id", mRtmInfo.appId);
        params.addProperty("room_id", FeedShareDataManger.getInstance().getRoomId());
        params.addProperty("user_id", FeedShareDataManger.getInstance().getUserId());
        params.addProperty("event_name", cmd);
        params.addProperty("request_id", UUID.randomUUID().toString());
        params.addProperty("device_id", SolutionDataManager.ins().getDeviceId());
        return params;
    }

    private boolean isNetworkDisabled() {
        if (!NetworkUtils.isNetworkAvailable(Utilities.getApplicationContext())) {
            SafeToast.show(Utilities.getApplicationContext(), "没有网络!", Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }

    private static final String CMD_BROADCAST_JOIN_ROOM = "twOnJoinRoom";
    private static final String CMD_BROADCAST_LEAVE_ROOM = "twOnLeaveRoom";
    private static final String CMD_BROADCAST_FINISH_ROOM = "twOnFinishRoom";
    private static final String CMD_BROADCAST_UPDATE_ROOM_SCENE = "twOnUpdateRoomScene";
    private static final String CMD_BROADCAST_CONTENT_UPDATE = "twOnContentUpdate";

    private void initEventListener() {
        //进入房间通知
        mEventListeners.put(CMD_BROADCAST_JOIN_ROOM, new AbsBroadcast<>(CMD_BROADCAST_JOIN_ROOM, JoinRoomInform.class, SolutionDemoEventManager::post));
        //离开房间通知
        mEventListeners.put(CMD_BROADCAST_LEAVE_ROOM, new AbsBroadcast<>(CMD_BROADCAST_LEAVE_ROOM, LeaveRoomInform.class, SolutionDemoEventManager::post));
        //结束房间通知
        mEventListeners.put(CMD_BROADCAST_FINISH_ROOM, new AbsBroadcast<>(CMD_BROADCAST_FINISH_ROOM, FinishRoomInform.class, SolutionDemoEventManager::post));
        //更新房间场景通知
        mEventListeners.put(CMD_BROADCAST_UPDATE_ROOM_SCENE, new AbsBroadcast<>(CMD_BROADCAST_UPDATE_ROOM_SCENE, UpdateRoomSceneInform.class, SolutionDemoEventManager::post));
        //内容更新通知
        mEventListeners.put(CMD_BROADCAST_CONTENT_UPDATE, new AbsBroadcast<>(CMD_BROADCAST_CONTENT_UPDATE, ContentUpdateInform.class, SolutionDemoEventManager::post));
    }
}
