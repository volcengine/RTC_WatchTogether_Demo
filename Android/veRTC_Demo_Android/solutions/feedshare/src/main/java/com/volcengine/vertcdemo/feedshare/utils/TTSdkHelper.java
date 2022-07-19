package com.volcengine.vertcdemo.feedshare.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.pandora.common.env.Env;
import com.pandora.ttlicense2.LicenseManager;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TTSdkHelper {
    private static final String TAG = "TTSdkHelper";
    private static final String TT_VIDEO_PLAYER_APP_ID = "260323";
    private static final String TT_VIDEO_PLAYER_APP_NAME = "vertcdemo";


    public static void initTTVodSdk() {
        initEnv();
        initLicense();
        initTTVideoEngine();
        initMDL();
        initDebug();
    }

    private static void initEnv() {
        // 初始化 TTSDK 环境
        Env.setupSDKEnv(new Env.SdkContextEnv() {
            @Override
            public Context getApplicationContext() {
                return Utilities.getApplicationContext();
            }

            @Override
            public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
                return (t, e) -> {
                };
            }

            @Override
            public String getAppID() {
                return TT_VIDEO_PLAYER_APP_ID;
            }

            @Override
            public String getAppName() {
                return TT_VIDEO_PLAYER_APP_NAME;
            }

            @Override
            public String getAppRegion() {
                return "china";
            }
        });
    }

    private static void initLicense() {
        //初始化 License 模块
        //初始化 license 2.0 的 LicenseManager
        LicenseManager.init(Utilities.getApplicationContext());
        //license 2.0 的授权文件支持从 assets 文件夹中直接读取，无需拷贝到存储
        String assetsLicenseUri = "assets:///ttvideo_player.lic";
        //将 license uri 添加到 LicenseManager 中即可完成授权文件添加
        LicenseManager.getInstance().addLicense(assetsLicenseUri, null);
    }

    private static void initTTVideoEngine() {
        //初始化TTVideoEngine&AppLog
        Map<String, Object> appInfo = new HashMap<>(5);
        appInfo.put("appname", TT_VIDEO_PLAYER_APP_NAME);
        appInfo.put("appid", TT_VIDEO_PLAYER_APP_ID); // your app id
        appInfo.put("appchannel", "xiaomi_appstore"); // 设为test_channel不会展示日志
        appInfo.put("region", "china");
        // FIXME: 2022/5/20
//        appInfo.put("appversion", BuildConfig.VERSION_NAME);
        // 初始化点播
        TTVideoEngine.setAppInfo(Utilities.getApplicationContext(), appInfo);
        // 初始化点播依赖的 AppLog SDK
        TTVideoEngine.initAppLog();
    }

    private static void initMDL() {
        File videoCacheDir = new File(Utilities.getApplicationContext().getCacheDir(), "video_cache");
        if (!videoCacheDir.exists()) {
            boolean result = videoCacheDir.mkdirs();
            if (!result) {
                Toast.makeText(Utilities.getApplicationContext(), "Invalid Cache Path", Toast.LENGTH_SHORT).show();
            }
        }
        TTVideoEngine.setStringValue(DataLoaderHelper.DATALOADER_KEY_STRING_CACHEDIR, videoCacheDir.getAbsolutePath());
        TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_INT_MAXCACHESIZE, 300 * 1024 * 1024); // 300MB
        try {
            // start MDL
            TTVideoEngine.startDataLoader(Utilities.getApplicationContext());
        } catch (Exception e) {
            Log.d(TAG, "initMDL fail:" + e.getMessage());
        }
    }

    private static void initDebug() {
        //开发的过程中，打开 logcat 日志，帮助定位问题。在 Release 版本一定要关闭
        TTVideoEngineLog.turnOn(TTVideoEngineLog.LOG_DEBUG, 1);// 1 打开 0 关闭
        //开启 License 模块 logcat 输出，排查问题可以开启，release 包不建议开启
        LicenseManager.turnOnLogcat(false);
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
