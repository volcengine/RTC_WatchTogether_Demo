// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.utils;

import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;
import com.pandora.ttlicense2.License;
import com.pandora.ttlicense2.LicenseManager;
import com.volcengine.vertcdemo.feedshare.BuildConfig;
import com.volcengine.vertcdemo.utils.AppUtil;

public class TTSdkHelper {
    private static final String TT_VIDEO_PLAYER_APP_ID = "260323";
    private static final String TT_VIDEO_PLAYER_APP_NAME = "vertcdemo";

    public static void initTTVodSdk() {
        // 初始化 TTSDK 环境
        Env.init(new Config.Builder()
                .setApplicationContext(AppUtil.getApplicationContext())
                .setAppID(TT_VIDEO_PLAYER_APP_ID)
                .setAppName(TT_VIDEO_PLAYER_APP_NAME)
                .setAppVersion(BuildConfig.APP_VERSION_NAME)
                .setAppChannel("RTCSDKDemo")
                .setLicenseUri("assets:///ttvideo_player.lic")
                .setLicenseCallback(new LogLicenseManagerCallback())
                .build());
    }

    public static boolean videoEnableH265() {
        return true;
    }

    public static boolean enablePreload() {
        return true;
    }

    public static boolean enableVideoHW() {
        return true;
    }

    public static boolean engineEnableUploadLog() {
        return true;
    }

    public static boolean mdlEnableUploadLog() {
        return true;
    }
}

/**
 * Log LicenseManager callback
 */
class LogLicenseManagerCallback implements LicenseManager.Callback {
    private static final String TAG = "TTSdkHelper";

    @Override
    public void onLicenseLoadSuccess(@NonNull String licenseUri, @NonNull String licenseId) {
        Log.d(TAG, "onLicenseLoadSuccess");
        printLicense(licenseId);
    }

    @Override
    public void onLicenseLoadError(@NonNull String licenseUri, @NonNull Exception e, boolean retryAble) {
        Log.d(TAG, "onLicenseLoadError:" + licenseUri + ", retryAble: " + retryAble, e);
    }

    @Override
    public void onLicenseLoadRetry(@NonNull String licenseUri) {
        Log.d(TAG, "onLicenseLoadRetry:" + licenseUri);
    }

    @Override
    public void onLicenseUpdateSuccess(@NonNull String licenseUri, @NonNull String licenseId) {
        Log.d(TAG, "onLicenseUpdateSuccess:" + licenseUri + ", licenseId=" + licenseId);
        printLicense(licenseId);
    }

    @Override
    public void onLicenseUpdateError(@NonNull String licenseUri, @NonNull Exception e, boolean retryAble) {
        Log.d(TAG, "onLicenseUpdateError:" + licenseUri + "," + retryAble, e);
    }

    @Override
    public void onLicenseUpdateRetry(@NonNull String licenseUri) {
        Log.d(TAG, "onLicenseUpdateRetry:" + licenseUri);
    }

    static void printLicense(String licenseId) {
        License license = LicenseManager.getInstance().getLicense(licenseId);
        if (license == null) {
            Log.d(TAG, "Failed to getLicense()");
            return;
        }

        Log.d(TAG, "License Info:");
        Log.d(TAG, " id: " + license.getId());
        Log.d(TAG, " package: " + license.getPackageName());
        Log.d(TAG, " type: " + license.getType());
        Log.d(TAG, " version: " + license.getVersion());

        final License.Module[] modules = license.getModules();
        if (modules != null) {
            Log.d(TAG, " modules: ");
            for (License.Module module : modules) {
                Log.d(TAG, "  + name: " + module.getName()
                        + ", start: " + DateFormat.format("yyyy-MM-dd kk:mm:ss", module.getStartTime())
                        + ", expire: " + DateFormat.format("yyyy-MM-dd kk:mm:ss", module.getExpireTime()));
            }
        } else {
            Log.d(TAG, " modules: none");
        }
    }
}
