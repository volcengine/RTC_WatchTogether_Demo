package com.volcengine.vertcdemo.feedshare.feature.effect;

import static com.volcengine.vertcdemo.utils.FileUtils.copyAssetFolder;

import android.text.TextUtils;
import android.util.Log;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EffectHelper {
    private RTCEngine mRtcEngine;
    private final ArrayList<String> mEffectPathList = new ArrayList<>();
    private final Map<String, Map<String, Float>> mEffectValue = new HashMap<>();
    private String mLastStickerPath = "";
    private String mLastFilter = "";
    private float mLastFilterValue = 0;

    public EffectHelper() {
    }

    public void setRtcEngine(RTCEngine rtcEngine) {
        mRtcEngine = rtcEngine;
    }

    public void initEffect() {
        initEffectPath();
        mEffectPathList.add(getByteComposePath());
        mEffectPathList.add(getByteShapePath());
        if (mRtcEngine != null) {
            int licRes = mRtcEngine.checkVideoEffectLicense(Utilities.getApplicationContext(), getLicensePath());
            Log.e("lzb ", "initEffect : " + licRes);
            mRtcEngine.setVideoEffectAlgoModelPath(getEffectAlgoModelPath());
            int enableRes = mRtcEngine.enableVideoEffect(true);
            mRtcEngine.setVideoEffectNodes(mEffectPathList);
            setStickerNodes(mLastStickerPath);
            updateVideoEffectNode();
            setVideoEffectColorFilter(mLastFilter);
            updateColorFilterIntensity(mLastFilterValue);
        }
    }

    public static void initEffectPath() {
        File licensePath = new File(getCacheResourcePath(), "cvlab/LicenseBag.bundle");
        if (!licensePath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/LicenseBag.bundle", licensePath.getAbsolutePath());
        }
        File modelPath = new File(getCacheResourcePath(), "cvlab/ModelResource.bundle");
        if (!modelPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ModelResource.bundle", modelPath.getAbsolutePath());
        }
        File stickerPath = new File(getCacheResourcePath(), "cvlab/StickerResource.bundle");
        if (!stickerPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/StickerResource.bundle", stickerPath.getAbsolutePath());
        }
        File filterPath = new File(getCacheResourcePath(), "cvlab/FilterResource.bundle");
        if (!filterPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/FilterResource.bundle", filterPath.getAbsolutePath());
        }
        File composerPath = new File(getCacheResourcePath(), "cvlab/ComposeMakeup.bundle");
        if (!composerPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ComposeMakeup.bundle", composerPath.getAbsolutePath());
        }
    }

    private static String getCacheResourcePath() {
        return Utilities.getApplicationContext().getCacheDir().getAbsolutePath()+ "/assets/resource/";
    }

    private static String getLicensePath() {
        return new File(getCacheResourcePath(), "cvlab/LicenseBag.bundle").getAbsolutePath() +
                "/rtc_test_20210911_20220831_rtc.vertcdemo.android_4.1.0.1.licbag";
    }

    private String getEffectAlgoModelPath() {
        return new File(getCacheResourcePath(), "cvlab/ModelResource.bundle").getAbsolutePath();
    }

    public static String getByteStickerPath() {
        File stickerPath = new File(getCacheResourcePath(), "cvlab/StickerResource.bundle");
        return stickerPath.getAbsolutePath() + "/";
    }

    public static String getByteComposePath() {
        File composerPath = new File(getCacheResourcePath(), "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath() + "/ComposeMakeup/beauty_Android_live";
    }

    public static String getByteShapePath() {
        File composerPath = new File(getCacheResourcePath(), "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath() + "/ComposeMakeup/reshape_live";
    }

    public static String getByteColorFilterPath() {
        File filterPath = new File(getCacheResourcePath(), "cvlab/FilterResource.bundle");
        return filterPath.getAbsolutePath() + "/Filter/";
    }


    public void updateVideoEffectNode() {
        if (mRtcEngine != null) {
            for (Map.Entry<String, Map<String, Float>> entry : mEffectValue.entrySet()) {
                String path = entry.getKey();
                Map<String, Float> keyValue = entry.getValue();
                for (Map.Entry<String, Float> temp : keyValue.entrySet()) {
                    updateVideoEffectNode(path, temp.getKey(), temp.getValue());
                }
            }
        }
    }

    public void updateVideoEffectNode(String path, String key, float val) {
        if (mRtcEngine != null) {
            int ret = mRtcEngine.updateVideoEffectNode(path, key, val);
        }
        Map<String, Float> keyValue = mEffectValue.get(path);
        if (keyValue == null) {
            keyValue = new HashMap<>();
            mEffectValue.put(path, keyValue);
        }
        keyValue.put(key, val);
    }

    public void setStickerNodes(String path) {
        if (mRtcEngine != null) {
            ArrayList<String> pathList = new ArrayList<>(mEffectPathList);
            if (!TextUtils.isEmpty(path)) {
                pathList.add(getByteStickerPath() + path);
            }
            mRtcEngine.setVideoEffectNodes(pathList);
        }
        mLastStickerPath = path;
    }

    public void setVideoEffectColorFilter(String path) {
        if (mRtcEngine != null) {
            mRtcEngine.setVideoEffectColorFilter(path);
        }
        mLastFilter = path;
    }

    public void updateColorFilterIntensity(float intensity) {
        if (mRtcEngine != null) {
            mRtcEngine.setVideoEffectColorFilterIntensity(intensity);
        }
        mLastFilterValue = intensity;
    }

    public String getStickerPath() {
        return mLastStickerPath;
    }


    /**** 一起看默认美颜效果 */
    public void setDefaultEffectForTW(){
        float defaultVal = EffectDialog.DEFAULT_PROGRESS/100f;
        updateVideoEffectNode(EffectHelper.getByteComposePath(), "whiten", defaultVal);
        updateVideoEffectNode(EffectHelper.getByteComposePath(), "smooth", defaultVal);
        updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Eye", defaultVal);
        updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Overall", defaultVal);
    }

}
