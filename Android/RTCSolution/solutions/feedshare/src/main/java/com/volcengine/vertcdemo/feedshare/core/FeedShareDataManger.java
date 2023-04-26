// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.core;

import static com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse.FEED_SHARE_ROOM_SCENE_CHAT;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.feedshare.bean.DeviceStatusRecord;
import com.volcengine.vertcdemo.feedshare.bean.JoinRoomResponse;
import com.volcengine.vertcdemo.utils.AppUtil;

import java.util.HashMap;

public class FeedShareDataManger {
    @JoinRoomResponse.ROOM_SCENE
    private int mCurScene;
    private String mRoomId;
    private String mHostUid;
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

    /**
     * 创建用户的渲染视图，一个用户对应一个view
     *
     * uid为空时，返回空对象
     *
     * @param userId 用户id
     * @return 用户视图
     */
    public @Nullable TextureView getUserRenderView(@NonNull String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        if (mTextures == null) {
            mTextures = new HashMap<>(3);
        }
        TextureView textureView = mTextures.get(userId);
        if (textureView == null) {
            textureView = new TextureView(AppUtil.getApplicationContext());
            mTextures.put(userId, textureView);
        }
        return textureView;
    }

    public void removeUserRenderView(String userId) {
        if (mTextures == null) {
            return;
        }
        mTextures.remove(userId);
    }

    public void clear() {
        mCurScene = FEED_SHARE_ROOM_SCENE_CHAT;

        DeviceStatusRecord.device.setMicOn();
        DeviceStatusRecord.device.setCameraOn();

        FeedShareRTCManager.getInstance().destroyEngine();

        mRoomId = null;
        mHostUid = null;

        if (mTextures != null) {
            mTextures.clear();
        }
        mTextures = null;
    }

    public int getCurScene() {
        return mCurScene;
    }

    public void setCurScene(@JoinRoomResponse.ROOM_SCENE int curScene) {
        this.mCurScene = curScene;
    }

    public String getUserId() {
        return SolutionDataManager.ins().getUserId();
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
        return TextUtils.equals(SolutionDataManager.ins().getUserId(), mHostUid);
    }
}
