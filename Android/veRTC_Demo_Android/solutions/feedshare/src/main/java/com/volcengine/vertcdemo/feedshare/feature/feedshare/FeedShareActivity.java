package com.volcengine.vertcdemo.feedshare.feature.feedshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rtm.RTMBaseClient;
import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.utils.MicCameraSwitchHelper;
import com.volcengine.vertcdemo.feedshare.core.FeedShareDataManger;
import com.volcengine.vertcdemo.feedshare.core.FeedShareRTMClient;
import com.volcengine.vertcdemo.feedshare.core.FeedShareRtcManager;
import com.volcengine.vertcdemo.feedshare.feature.AudioControllerDialog;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectDialog;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectHelper;
import com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse;
import com.volcengine.vertcdemo.feedshare.bean.VideoItem;
import com.volcengine.vertcdemo.feedshare.bean.VideoResponse;
import com.volcengine.vertcdemo.feedshare.event.ContentUpdateInform;
import com.volcengine.vertcdemo.feedshare.event.FinishRoomInform;
import com.volcengine.vertcdemo.feedshare.event.RTCErrorEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCNetStatusEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserJoinEvent;
import com.volcengine.vertcdemo.feedshare.event.RTCUserLeaveEvent;
import com.volcengine.vertcdemo.feedshare.event.UpdateRoomSceneInform;
import com.volcengine.vertcdemo.feedshare.utils.VodAudioProcessor;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedShareActivity extends BaseActivity {
    private static final String TAG = "FeedShareActivity";
    private VideoFragment mVideosFragment;

    private CameraId mCameraID = CameraId.CAMERA_ID_FRONT;
    private TextView mNetStatus;
    private Group mChatViews;
    private Group mShareViews;
    private FrameLayout mChatSelfContainer;
    private FrameLayout mChatRemoteContainer1;
    private FrameLayout mChatRemoteContainer2;
    private FrameLayout mShareSelfContainer;
    private FrameLayout mShareRemoteContainer1;
    private FrameLayout mShareRemoteContainer2;
    private MicCameraSwitchHelper micCameraSwitchHelper;
    private List<String> mRemoteUserIds;
    private final List<VideoItem> mRoomContentList = new ArrayList<>();

    private final ISyncHandler mRTCMsgSyncHandler = new SyncHandlerAdapter() {
        @Override
        public void handleRequestFeedShare(String peerUid) {
            if (FeedShareDataManger.getInstance().isHost()) {
                startFeedShare();
            }
        }
    };

    public static void start(Activity context) {
        Intent intent = new Intent(context, FeedShareActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_share);

        initUI();
        joinRoom();
        SolutionDemoEventManager.register(this);
        FeedShareRtcManager.getInstance().addSyncHandler(mRTCMsgSyncHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SolutionDemoEventManager.unregister(this);
        FeedShareRtcManager.getInstance().removeSyncHandler(mRTCMsgSyncHandler);
    }

    private void initUI() {
        mChatViews = findViewById(R.id.chat_views);
        mShareViews = findViewById(R.id.share_views);
        mChatSelfContainer = findViewById(R.id.chat_self_container);
        mChatRemoteContainer1 = findViewById(R.id.chat_remote_user_1);
        mChatRemoteContainer2 = findViewById(R.id.chat_remote_user_2);
        mShareSelfContainer = findViewById(R.id.share_self_container);
        mShareRemoteContainer1 = findViewById(R.id.share_remote_user_1);
        mShareRemoteContainer2 = findViewById(R.id.share_remote_user_2);
        ImageView micSwitchIv = findViewById(R.id.mic_switch);
        micSwitchIv.setOnClickListener((v) -> toggleLocalAudio());
        ImageView cameraSwitchIv = findViewById(R.id.camera_switch);
        cameraSwitchIv.setOnClickListener((v) -> toggleLocalVideo());
        findViewById(R.id.effect_setting).setOnClickListener(v -> openVideoEffectDialog());
        findViewById(R.id.setting_iv).setOnClickListener(v -> openSetting());
        findViewById(R.id.feed_share_iv).setOnClickListener(v -> startFeedShare());
        findViewById(R.id.hangup_iv).setOnClickListener(v -> {
            boolean isHost = FeedShareDataManger.getInstance().isHost();
            int curScene = FeedShareDataManger.getInstance().getCurScene();
            if (isHost && curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE) {
                updateRoomSceneValueAndUi(JoinRoomResponse.FEED_SHARE_ROOM_SCENE_CHAT, true);
            } else {
                finish();
                requestLeaveRoom();
            }
        });
        findViewById(R.id.change_camera).setOnClickListener((v) -> onChangeCameraClick());

        mNetStatus = findViewById(R.id.local_net_status_tv);
        mNetStatus = findViewById(R.id.local_net_status_tv);

        TextView roomIDTV = findViewById(R.id.room_id_tv);
        roomIDTV.setText(String.format("RoomID:%s", FeedShareDataManger.getInstance().getRoomId()));

        micCameraSwitchHelper = new MicCameraSwitchHelper();
        micCameraSwitchHelper.setEngine(getEngine());
        micCameraSwitchHelper.setMicBtn(micSwitchIv);
        micCameraSwitchHelper.setCameraBtn(cameraSwitchIv);
        micCameraSwitchHelper.updateMicAndCameraUI();
        micCameraSwitchHelper.updateRtcAudioAndVideoCapture();
    }

    private void startFeedShare() {
        if (FeedShareDataManger.getInstance().isHost()) {
            if (mRoomContentList != null && mRoomContentList.size() > 0) {
                updateRoomSceneValueAndUi(JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE, true);
                return;
            }
            requestVideos(new IRequestCallback<VideoResponse>() {

                @Override
                public void onSuccess(VideoResponse data) {
                    if (isFinishing()) return;
                    if (data == null || data.videoItemList == null) {
                        return;
                    }
                    mRoomContentList.addAll(data.videoItemList);
                    updateRoomSceneValueAndUi(JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE, true);
                }

                @Override
                public void onError(int errorCode, String message) {
                    Log.i(TAG, "startVideoShare but requestVideos onError errorCode:" + errorCode + ",message:" + message);
                }
            });
        } else {
            String hostUid = FeedShareDataManger.getInstance().getHostUid();
            String msg = SyncMessageUtil.createRequestShareFeedMessage();
            FeedShareRtcManager.getInstance().sendMessageToUser(hostUid, msg);
        }
    }

    /**
     * 更新房间场景值和相关UI
     *
     * @param roomScene        新的房间场景值
     * @param needUpdateServer 是否需要更新服务端场景数据
     */
    private void updateRoomSceneValueAndUi(@JoinRoomResponse.ROOM_SCENE int roomScene, boolean needUpdateServer) {
        int targetScene = roomScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE ?
                JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE :
                JoinRoomResponse.FEED_SHARE_ROOM_SCENE_CHAT;
        if (!needUpdateServer) {
            FeedShareDataManger.getInstance().setCurScene(targetScene);
            bindViewForScene();
            return;
        }
        FeedShareRTMClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null) {
            return;
        }
        rtmClient.updateRoomScene(new IRequestCallback<Object>() {

            @Override
            public void onSuccess(Object data) {
                FeedShareDataManger.getInstance().setCurScene(targetScene);
                bindViewForScene();
            }

            @Override
            public void onError(int errorCode, String message) {
                Log.i(TAG, "updateRoomScene onError errorCode:" + errorCode + ",message:" + message);
            }
        }, targetScene);
    }

    /*** 切换前置/后置摄像头（默认使用前置摄像头）*/
    private void onChangeCameraClick() {
        RTCEngine engine = getEngine();
        boolean isFront = mCameraID.equals(CameraId.CAMERA_ID_FRONT);
        mCameraID = isFront ? CameraId.CAMERA_ID_BACK : CameraId.CAMERA_ID_FRONT;
        engine.setLocalVideoMirrorType(isFront ? MirrorType.MIRROR_TYPE_NONE : MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER);
        engine.switchCamera(mCameraID);
    }

    private void toggleLocalAudio() {
        if (micCameraSwitchHelper != null) {
            micCameraSwitchHelper.toggleMic();
        }
    }

    private void toggleLocalVideo() {
        if (micCameraSwitchHelper != null) {
            micCameraSwitchHelper.toggleCamera();
        }
    }

    private void openVideoEffectDialog() {
        EffectDialog effectDialog = new EffectDialog(this);
        EffectHelper helper = FeedShareDataManger.getInstance().getEffectHelper();
        effectDialog.setCallBack(new EffectDialog.AdjustCallBack() {
            @Override
            public void updateVideoEffectNode(String path, String key, float val) {
                helper.updateVideoEffectNode(path, key, val);
            }

            @Override
            public void setVideoEffectColorFilter(String path) {
                helper.setVideoEffectColorFilter(path);
            }

            @Override
            public void updateColorFilterIntensity(float intensity) {
                helper.updateColorFilterIntensity(intensity);
            }

            @Override
            public void setStickerNodes(String path) {
                helper.setStickerNodes(path);
            }

            @Override
            public void reset() {
                effectDialog.setDefaultProgress(0);
            }
        });
        effectDialog.show();
    }

    private int mVideoAudioGain = 20;
    private int mRtcAudioGain = 100;

    private RTCEngine getEngine() {
        return FeedShareRtcManager.getInstance().getEngine();
    }

    public void openSetting() {
        AudioControllerDialog audioDialog = new AudioControllerDialog(this);
        audioDialog.setVideoDefault(mVideoAudioGain);
        audioDialog.setRtcDefault(mRtcAudioGain);
        audioDialog.setAudioChangeListener(new AudioControllerDialog.AudioChangeListener() {
            @Override
            public void onVideoAudioChange(int progress) {
                RTCEngine engine = getEngine();
                if (engine == null) {
                    return;
                }
                VodAudioProcessor.mixAudioGain = progress;
            }

            @Override
            public void onVideoStopTracking(int progress) {
                mVideoAudioGain = progress;
            }

            @Override
            public void onRtcAudioChange(int progress) {
                RTCEngine engine = getEngine();
                if (engine == null) {
                    return;
                }
                engine.setPlaybackVolume(progress);
            }

            @Override
            public void onRtcStopTracking(int progress) {
                mRtcAudioGain = progress;
            }
        });
        audioDialog.show();
    }

    private void joinRoom() {
        FeedShareRTMClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null) {
            return;
        }
        //业务入房，如果没有房间创建一个房间
        rtmClient.joinRoom(new IRequestCallback<JoinRoomResponse>() {
            @Override
            public void onSuccess(JoinRoomResponse data) {
                if (isFinishing()) {
                    return;
                }
                if (data == null || TextUtils.isEmpty(data.rtcToken)) {
                    return;
                }
                FeedShareDataManger.getInstance().setHostUid(data.hostUid);
                String userId = FeedShareDataManger.getInstance().getUserId();
                String roomId = FeedShareDataManger.getInstance().getRoomId();
                //RTC入房
                RTCRoomConfig roomConfig = new RTCRoomConfig(RTCEngine.ChannelProfile.CHANNEL_PROFILE_COMMUNICATION, true, true, true);
                RTCEngine engine = getEngine();
                int joinRoomResult = engine.joinRoom(data.rtcToken, roomId, UserInfo.create(userId, ""), roomConfig);
                int videoSize = data.contentList == null ? 0 : data.contentList.size();
                if (joinRoomResult == 0) {
                    if (videoSize > 0) {
                        mRoomContentList.addAll(data.contentList);
                    }
                    updateRoomSceneValueAndUi(data.roomScene, false);
                } else {
                    Log.i(TAG, "joinRoom joinRoomResult != 0");
                }
                //房主预加载视频数据
                if (TextUtils.equals(data.hostUid, userId) && videoSize == 0) {
                    requestVideos(new IRequestCallback<VideoResponse>() {
                        @Override
                        public void onSuccess(VideoResponse data) {
                            int videoSize = data.videoItemList == null ? 0 : data.videoItemList.size();
                            if (videoSize > 0) {
                                mRoomContentList.addAll(data.videoItemList);
                            }
                        }

                        @Override
                        public void onError(int errorCode, String message) {
                        }
                    });
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                if (RTMBaseClient.ERROR_CODE_USERNAME_SAME == errorCode) {
                    SafeToast.show(FeedShareActivity.this, "该用户已存在房间中", Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
                if (RTMBaseClient.ERROR_CODE_ROOM_FULL == errorCode) {
                    SafeToast.show(FeedShareActivity.this, "房间人数已满", Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
                Log.i(TAG, "joinRoom biz failed message: " + message + ",errorCode:" + errorCode);
            }
        });
    }

    /***根据场景值更新UI*/
    private void bindViewForScene() {
        int curScene = FeedShareDataManger.getInstance().getCurScene();
        boolean shareViewVisible = curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE;
        mChatViews.setVisibility(shareViewVisible ? View.GONE : View.VISIBLE);
        mShareViews.setVisibility(shareViewVisible ? View.VISIBLE : View.GONE);
        setLocalRenderView();
        if (mRemoteUserIds != null && mRemoteUserIds.size() > 0) {
            for (String uid : mRemoteUserIds) {
                setRemoteView(uid);
            }
        }
        if (curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE) {
            showVideoFragment();
        } else {
            removeVideoFragment();
        }
    }

    /***设置自己视频渲染*/
    private void setLocalRenderView() {
        int curScene = FeedShareDataManger.getInstance().getCurScene();
        String userId = FeedShareDataManger.getInstance().getUserId();
        FrameLayout localContainer = curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE
                ? mShareSelfContainer
                : mChatSelfContainer;
        Log.i(TAG, "setLocalRenderView isChat:" + (localContainer == mChatSelfContainer));
        attachRenderView(localContainer, userId, false);
    }

    /***设置远端用户视频渲染*/
    private void setRemoteView(String userId) {
        if (TextUtils.isEmpty(userId)) return;
        int curScene = FeedShareDataManger.getInstance().getCurScene();
        FrameLayout remoteContainer1 = curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE
                ? mShareRemoteContainer1
                : mChatRemoteContainer1;
        FrameLayout remoteContainer2 = curScene == JoinRoomResponse.FEED_SHARE_ROOM_SCENE_FEED_SHARE
                ? mShareRemoteContainer2
                : mChatRemoteContainer2;
        int container1Child = remoteContainer1.getChildCount();
        int container2Child = remoteContainer2.getChildCount();
        FrameLayout remoteContainer = null;
        if (container1Child == 0) {
            remoteContainer = remoteContainer1;
        }
        if (remoteContainer == null && container2Child == 0) {
            remoteContainer = remoteContainer2;
        }
        if (remoteContainer != null) {
            attachRenderView(remoteContainer, userId, true);
        }
    }

    private void attachRenderView(FrameLayout parentView, String uid, boolean isRemoteUser) {
        final RTCEngine engine = getEngine();
        if (parentView == null || TextUtils.isEmpty(uid) || engine == null) {
            return;
        }
        VideoCanvas videoCanvas = new VideoCanvas();
        videoCanvas.uid = uid;
        videoCanvas.isScreen = false;
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN;
        videoCanvas.renderView = FeedShareDataManger.getInstance().getUserRenderView(uid);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        parentView.removeAllViews();
        parentView.addView(videoCanvas.renderView, params);
        parentView.setVisibility(View.VISIBLE);
        if (isRemoteUser) {
            engine.setRemoteVideoCanvas(uid, StreamIndex.STREAM_INDEX_MAIN, null);
            engine.setRemoteVideoCanvas(uid, StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
        } else {
            engine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, null);
            engine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
        }
    }

    private void requestVideos(IRequestCallback<VideoResponse> callback) {
        FeedShareRTMClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null) {
            return;
        }
        rtmClient.getContentList(callback);
    }

    private void showVideoFragment() {
        if (mVideosFragment != null && mVideosFragment.isVisible()) return;
        if (mRoomContentList != null && mRoomContentList.size() > 0) {
            mVideosFragment = new VideoFragment();
            mVideosFragment.setInitVideos(new ArrayList<>(mRoomContentList));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.share_videos_container, mVideosFragment)
                    .commitAllowingStateLoss();
            mRoomContentList.clear();
        }
    }

    private void removeVideoFragment() {
        if (mVideosFragment == null) return;
        getSupportFragmentManager().beginTransaction()
                .remove(mVideosFragment)
                .commitAllowingStateLoss();
        mVideosFragment = null;
    }

    /**
     * 用户离开时移除View
     **/
    private void removeRemoteView(String uid) {
        FeedShareDataManger.getInstance().getUserRenderView(uid);
        FeedShareDataManger.getInstance().removeUserRenderView(uid);
    }

    /**
     * 提示弹框
     **/
    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("知道了", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetStatus(RTCNetStatusEvent stats) {
        if (isFinishing() || stats == null) return;
        mNetStatus.setText(stats.unblocked ? "网络良好" : "网络卡顿");
        Drawable drawable = ContextCompat.getDrawable(this, stats.unblocked ? R.drawable.net_status_good : R.drawable.net_status_bad);
        if (drawable == null) return;
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mNetStatus.setCompoundDrawables(drawable, null, null, null);
    }

    /**
     * RTC 出错提示
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onError(RTCErrorEvent err) {
        if (isFinishing() || err == null) return;
        showAlertDialog(String.format(Locale.US, "error: %d", err.errorCode));
    }

    /**
     * RTC 用户离开事件回调
     *
     * @param userLeaveEvent 离开用户信息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserLeave(RTCUserLeaveEvent userLeaveEvent) {
        if (isFinishing() || userLeaveEvent == null) return;
        Log.i(TAG, "onUserLeave uid:" + userLeaveEvent.userId);
        mRemoteUserIds.remove(userLeaveEvent.userId);
        removeRemoteView(userLeaveEvent.userId);
    }

    /**
     * RTC 远端用户进入入房事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserJoin(RTCUserJoinEvent userJoinEvent) {
        if (isFinishing() || userJoinEvent == null) return;
        Log.i(TAG, "onUserJoin uid:" + userJoinEvent.userId);
        if (mRemoteUserIds == null) {
            mRemoteUserIds = new ArrayList<>(1);
        }
        mRemoteUserIds.add(userJoinEvent.userId);
        setRemoteView(userJoinEvent.userId);
    }

    /**
     * 结束房间事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishRoom(FinishRoomInform finishRoomEvent) {
        if (isFinishing() || finishRoomEvent == null) return;
        Log.i(TAG, "onFinishRoom uid:" + finishRoomEvent.roomId);
        if (TextUtils.equals(finishRoomEvent.roomId, FeedShareDataManger.getInstance().getRoomId())) {
            if (!FeedShareDataManger.getInstance().isHost()) {
                SafeToast.show(FeedShareActivity.this, "房主已关闭房间", Toast.LENGTH_SHORT);
            }
            RTCEngine engine = getEngine();
            if (engine != null) {
                engine.leaveRoom();
            }
            finish();
        }
    }

    /**
     * 更新房间场景
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRoomScene(UpdateRoomSceneInform updateRoomSceneEvent) {
        if (isFinishing() || updateRoomSceneEvent == null) return;
        Log.i(TAG, "FeedShareActivity onUpdateRoomScene:" + updateRoomSceneEvent.roomScene);
        if (!FeedShareDataManger.getInstance().isHost()) {
            updateRoomSceneValueAndUi(updateRoomSceneEvent.roomScene, false);
        }
    }

    /**
     * 首次收到同步视频数据事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateVideos(ContentUpdateInform contentUpdateEvent) {
        if (contentUpdateEvent == null || contentUpdateEvent.contentList == null) {
            return;
        }
        Log.i(TAG, "FeedShareActivity onUpdateVideos:" + contentUpdateEvent.contentList.size());
        if (FeedShareDataManger.getInstance().isHost() || //主播自己请求数据
                (mVideosFragment != null && mVideosFragment.isVisible())) { // 观众在非一起看场景下需要记录视频数据
            return;
        }
        mRoomContentList.addAll(contentUpdateEvent.contentList);
    }

    private void requestLeaveRoom() {
        FeedShareRTMClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null) {
            return;
        }
        IRequestCallback<Object> callback = new IRequestCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                RTCEngine engine = getEngine();
                if (engine != null) {
                    engine.leaveRoom();
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                Log.e(TAG, "finish error msg:" + message + ",code:" + errorCode);
            }
        };
        rtmClient.leaveRoom(callback);
    }
}
