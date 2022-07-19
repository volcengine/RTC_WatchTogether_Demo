package com.volcengine.vertcdemo.feedshare.core;

import static com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse.FEED_SHARE_ROOM_SCENE_CHAT;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.feedshare.bean.DeviceStatusRecord;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectHelper;
import com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse;

import java.util.HashMap;

public class FeedShareDataManger {
    @JoinRoomResponse.ROOM_SCENE
    private int mCurScene;
    private String mUserId;
    private String mRoomId;
    private String mHostUid;
    private EffectHelper mEffectHelper;
    private HashMap<String, TextureView> mTextures;

    private FeedShareDataManger() {
    }

    private static class Inner {
        @SuppressLint("StaticFieldLeak")
        private static final FeedShareDataManger sInstance = new FeedShareDataManger();
    }

    public static FeedShareDataManger getInstance() {
        return Inner.sInstance;
    }

    public void initEffectHelper() {
        RTCEngine engine = FeedShareRtcManager.getInstance().getEngine();
        if (engine == null) {
            throw new IllegalStateException("RTCEngine is null when init EffectHelper!");
        }
        mEffectHelper = new EffectHelper();
        mEffectHelper.setRtcEngine(engine);
        mEffectHelper.initEffect();
        mEffectHelper.setDefaultEffectForTW();
    }

    public TextureView getUserRenderView(String userId) {
        if (TextUtils.isEmpty(userId)) return null;
        if (mTextures == null) {
            mTextures = new HashMap<>(3);
        }
        TextureView textureView = mTextures.get(userId);
        if (textureView == null) {
            textureView = new TextureView(Utilities.getApplicationContext());
            mTextures.put(userId, textureView);
        }
        ViewParent parent = textureView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(textureView);
            ((ViewGroup) parent).setVisibility(View.GONE);
        }
        return textureView;
    }

    public void removeUserRenderView(String userId) {
        if (mTextures == null) return;
        mTextures.remove(userId);
    }

    public void clear() {
        mCurScene = FEED_SHARE_ROOM_SCENE_CHAT;

        DeviceStatusRecord.device.setMicOn();
        DeviceStatusRecord.device.setCameraOn();

        FeedShareRtcManager.getInstance().clear();

        mRoomId = null;
        mHostUid = null;

        if (mTextures != null) {
            mTextures.clear();
        }
        mTextures = null;
    }

    public EffectHelper getEffectHelper() {
        return mEffectHelper;
    }

    public int getCurScene() {
        return mCurScene;
    }

    public void setCurScene(@JoinRoomResponse.ROOM_SCENE int curScene) {
        this.mCurScene = curScene;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public String getHostUid() {
        return mHostUid;
    }

    public void setHostUid(String hostUid) {
        mHostUid = hostUid;
    }

    public boolean isHost() {
        return TextUtils.equals(mUserId, mHostUid);
    }
}
