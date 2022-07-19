/*
 * Copyright 2021 bytedance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Create Date : 2021/2/28
 */
package com.volcengine.vertcdemo.feedshare.view.layers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import com.volcengine.vertcdemo.feedshare.view.layer.BaseVideoLayer;
import com.volcengine.vertcdemo.feedshare.view.layer.ILayer;
import com.volcengine.vertcdemo.feedshare.view.layer.IVideoLayerEvent;

import java.util.Collections;
import java.util.List;

public class CoverLayer extends BaseVideoLayer {
    private static final String TAG = "CoverLayer";

    private ImageView mCoverView;

    private final List<Integer> mSupportEvents =
            Collections.singletonList(IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START);

    @Override
    public int getZIndex() {
        return ILayer.VIDEO_COVER_Z_INDEX;
    }

    @Override
    protected View getLayerView(final Context context, @NonNull LayoutInflater inflater) {
        mCoverView = new ImageView(context);
        mCoverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mCoverView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        return mCoverView;
    }

    @Override
    public void refresh() {
        show();
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return mSupportEvents;
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    private void show() {
        Log.d(TAG, "show");
        if (mCoverView == null) {
            return;
        }
        mCoverView.setVisibility(View.VISIBLE);
        if (mHost == null) {
            return;
        }
        final String cover = mHost.getCover();
        Glide.with(mCoverView).load(cover).into(mCoverView);
    }

    private void hide() {
        TTVideoEngineLog.d(TAG, "hide");
        if (mCoverView == null) {
            return;
        }
        mCoverView.setVisibility(View.GONE);
    }
}
