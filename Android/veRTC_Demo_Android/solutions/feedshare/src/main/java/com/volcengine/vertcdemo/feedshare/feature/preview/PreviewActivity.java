package com.volcengine.vertcdemo.feedshare.feature.preview;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoStreamDescription;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.adapter.TextWatcherAdapter;
import com.ss.video.rtc.demo.basic_module.ui.CommonDialog;
import com.ss.video.rtc.demo.basic_module.utils.IMEUtils;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.common.LengthFilterWithCallback;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.rtm.RTMBaseClient;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.utils.MicCameraSwitchHelper;
import com.volcengine.vertcdemo.feedshare.core.FeedShareDataManger;
import com.volcengine.vertcdemo.feedshare.core.FeedShareRtcManager;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectDialog;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectHelper;
import com.volcengine.vertcdemo.feedshare.feature.feedshare.FeedShareActivity;
import com.volcengine.vertcdemo.feedshare.utils.FeedShareConstants;
import com.volcengine.vertcdemo.feedshare.utils.TTSdkHelper;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * 一起看Demo预览&登陆页
 */
public class PreviewActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "[TW]PreviewActivity";
    public static final String ROOM_INPUT_REGEX = "^[a-zA-Z0-9@_-]+$";
    public static final String SCENE_NAME = "tw";

    private boolean mRoomIdOverflow = false;
    private View mRootView;
    private FrameLayout mPreviewContainer;
    private EditText mRoomIdEt;
    private TextView mRoomIdErrorTv;
    private TextView mJoinRoom;
    private ImageView mCameraStatus;
    private ImageView mCameraSwitch;
    private ImageView mMicSwitch;
    private ImageView mEffectSetting;
    private ImageView mCloseIv;
    private RTCEngine mRtcEngine;

    public static void start(Context context, @NonNull RtmInfo rtmInfo) {
        if (!rtmInfo.isValid()) {
            Log.e(TAG, "rtmInfo is not valid.");
            return;
        }
        FeedShareRtcManager.getInstance().init(rtmInfo);
        RTMBaseClient rtmClient = FeedShareRtcManager.getInstance().getRTMClient();
        if (rtmClient == null) {
            return;
        }

        rtmClient.login(rtmInfo.rtmToken,
                (resultCode, message) -> {
                    if (resultCode == RTMBaseClient.LoginCallBack.SUCCESS) {
                        Intent intent = new Intent(context, PreviewActivity.class);
                        context.startActivity(intent);
                    } else {
                        SafeToast.show("Login RTM Fail Error:" + resultCode + ",message:" + message);
                    }
                });
    }

    private final Runnable mRoomIdDismissRunnable = () -> mRoomIdErrorTv.setVisibility(View.GONE);

    private final TextWatcherAdapter mTextWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            setupInputStatus();
        }
    };

    private MicCameraSwitchHelper micCameraSwitchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TTSdkHelper.initTTVodSdk();
        setContentView(R.layout.activity_feed_share_preview);
        initUi();
        micCameraSwitchHelper = new MicCameraSwitchHelper();
        micCameraSwitchHelper.setMicBtn(mMicSwitch);
        micCameraSwitchHelper.setCameraBtn(mCameraSwitch);
        micCameraSwitchHelper.setStatusView(mCameraStatus);
        initRtc();
        if (micCameraSwitchHelper != null) {
            micCameraSwitchHelper.setEngine(mRtcEngine);
            micCameraSwitchHelper.updateMicAndCameraUI();
            micCameraSwitchHelper.updateRtcAudioAndVideoCapture();
        }
        FeedShareDataManger.getInstance().initEffectHelper();
    }

    private void initRtc() {
        mRtcEngine = FeedShareRtcManager.getInstance().getEngine();
        // 设置视频参数
        VideoStreamDescription description = new VideoStreamDescription();
        description.videoSize = new Pair<>(FeedShareConstants.VIDEO_SIZE_WIDTH, FeedShareConstants.VIDEO_SIZE_HEIGHT);
        description.frameRate = FeedShareConstants.VIDEO_FRAME_RATE;
        description.maxKbps = FeedShareConstants.VIDEO_MAX_KBPS;
        mRtcEngine.setVideoEncoderConfig(Collections.singletonList(description));
        mRtcEngine.setLocalVideoMirrorType(MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER);
        setLocalRenderView();
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    protected void initUi() {
        mRootView = findViewById(R.id.root_fl);
        mPreviewContainer = findViewById(R.id.preview_container);
        mCloseIv = findViewById(R.id.close_iv);
        mRoomIdEt = findViewById(R.id.room_id_et);
        mRoomIdErrorTv = findViewById(R.id.room_id_waring_tv);
        mJoinRoom = findViewById(R.id.join_room);
        mCameraStatus = findViewById(R.id.camera_status);
        mCameraSwitch = findViewById(R.id.camera_switch);
        mMicSwitch = findViewById(R.id.mic_switch);
        mEffectSetting = findViewById(R.id.effect_setting);

        mRootView.setOnClickListener(this);
        mJoinRoom.setOnClickListener(this);
        mCameraSwitch.setOnClickListener(this);
        mMicSwitch.setOnClickListener(this);
        mEffectSetting.setOnClickListener(this);
        mCloseIv.setOnClickListener(this);

        mRoomIdEt.addTextChangedListener(mTextWatcher);
        InputFilter meetingIDFilter = new LengthFilterWithCallback(18, (overflow) -> mRoomIdOverflow = overflow);
        InputFilter[] meetingIDFilters = new InputFilter[]{meetingIDFilter};
        mRoomIdEt.setFilters(meetingIDFilters);

        requestPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
        showLimitedServiceDialog();
    }

    private void setLocalRenderView() {
        VideoCanvas videoCanvas = new VideoCanvas();
        videoCanvas.isScreen = false;
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN;
        String selfUid = SolutionDataManager.ins().getUserId();
        if (TextUtils.isEmpty(selfUid)) return;
        videoCanvas.renderView = FeedShareDataManger.getInstance().getUserRenderView(selfUid);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mPreviewContainer.removeAllViews();
        mPreviewContainer.addView(videoCanvas.renderView, params);
        mPreviewContainer.setVisibility(View.VISIBLE);
        mRtcEngine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, null);
        mRtcEngine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
    }

    @Override
    public void onClick(View v) {
        if (v == mJoinRoom) {
            if (mRtcEngine == null) {
                SafeToast.show(this, "需要先初始化RTC引擎", Toast.LENGTH_SHORT);
                return;
            }
            String roomId = getRoomId();
            if (TextUtils.isEmpty(roomId)) {
                SafeToast.show(this, "房间号不能为空", Toast.LENGTH_SHORT);
                return;
            }
            FeedShareDataManger.getInstance().setUserId(SolutionDataManager.ins().getUserId());
            FeedShareDataManger.getInstance().setRoomId(roomId);
            FeedShareActivity.start(this);
        } else if (v == mCameraSwitch) {
            if (micCameraSwitchHelper != null) {
                micCameraSwitchHelper.toggleCamera();
            }
        } else if (v == mMicSwitch) {
            if (micCameraSwitchHelper != null) {
                micCameraSwitchHelper.toggleMic();
            }
        } else if (v == mEffectSetting) {
            openVideoEffectDialog();
        } else if (v == mRootView) {
            IMEUtils.closeIME(mRootView);
        } else if (v == mCloseIv) {
            FeedShareDataManger.getInstance().clear();
            finish();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mRtcEngine != null) {
            mRtcEngine.setLocalVideoMirrorType(MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER);
            mRtcEngine.switchCamera(CameraId.CAMERA_ID_FRONT);
            int childCount = mPreviewContainer.getChildCount();
            if (childCount == 0) {
                setLocalRenderView();
            }
        }
        if (micCameraSwitchHelper != null) {
            micCameraSwitchHelper.updateMicAndCameraUI();
            micCameraSwitchHelper.updateRtcAudioAndVideoCapture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED) {
            if (micCameraSwitchHelper != null) {
                micCameraSwitchHelper.openMic();
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            if (micCameraSwitchHelper != null) {
                micCameraSwitchHelper.openCamera();
            }
        }
    }

    private void showLimitedServiceDialog() {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.login_limited_service));
        dialog.setPositiveListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupInputStatus() {
        int roomIDLength = mRoomIdEt.getText().length();
        boolean canJoin = false;
        if (Pattern.matches(ROOM_INPUT_REGEX, mRoomIdEt.getText().toString())) {
            if (mRoomIdOverflow) {
                mRoomIdErrorTv.setVisibility(View.VISIBLE);
                mRoomIdErrorTv.setText(R.string.login_input_feed_share_id_waring);
                mRoomIdErrorTv.removeCallbacks(mRoomIdDismissRunnable);
                mRoomIdErrorTv.postDelayed(mRoomIdDismissRunnable, 2500);
            } else {
                mRoomIdErrorTv.setVisibility(View.INVISIBLE);
                canJoin = true;
            }
        } else {
            if (roomIDLength > 0) {
                mRoomIdErrorTv.setVisibility(View.VISIBLE);
                mRoomIdErrorTv.setText(R.string.login_input_wrong_content_waring);
            } else {
                mRoomIdErrorTv.setVisibility(View.INVISIBLE);
            }
        }

        boolean joinBtnEnable = roomIDLength > 0 && roomIDLength <= 18 && canJoin;
        mJoinRoom.setEnabled(joinBtnEnable);
    }

    public String getRoomId() {
        return mRoomIdEt.getText().toString().trim();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FeedShareDataManger.getInstance().clear();
    }
}
