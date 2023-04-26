// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.feature;

import static com.volcengine.vertcdemo.core.net.rts.RTSInfo.KEY_RTS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;

import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.utils.Utils;
import com.volcengine.vertcdemo.common.WindowUtils;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.feedshare.core.Constants;
import com.volcengine.vertcdemo.feedshare.feature.preview.PreviewActivity;

public class FeedShareEntryActivity extends SolutionBaseActivity {

    private static final String TAG = "FeedShareEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RTSInfo rtmInfo = getIntent() == null ? null : getIntent().getParcelableExtra(RTSInfo.KEY_RTS);
        PreviewActivity.start(FeedShareEntryActivity.this, rtmInfo);
        finish();
    }

    @Keep
    @SuppressWarnings("unused")
    public static void prepareSolutionParams(Activity activity, IAction<Object> doneAction) {
        Log.d(TAG, "prepareSolutionParams() invoked");
        IRequestCallback<ServerResponse<RTSInfo>> callback = new IRequestCallback<ServerResponse<RTSInfo>>() {
            @Override
            public void onSuccess(ServerResponse<RTSInfo> response) {
                RTSInfo data = response == null ? null : response.getData();
                if (data == null || !data.isValid()) {
                    onError(-1, "");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(AppUtil.getApplicationContext(), FeedShareEntryActivity.class);
                intent.putExtra(KEY_RTS, data);
                activity.startActivity(intent);
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }
        };
        FeedShareJoinRTSRequest request = new FeedShareJoinRTSRequest();
        JoinRTSManager.setAppInfoAndJoinRTM(request, callback);
    }
}