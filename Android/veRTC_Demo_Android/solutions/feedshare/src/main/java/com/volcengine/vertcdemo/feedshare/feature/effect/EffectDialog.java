package com.volcengine.vertcdemo.feedshare.feature.effect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.feature.effect.customview.EffectBeautyLayout;
import com.volcengine.vertcdemo.feedshare.feature.effect.customview.EffectFilterLayout;
import com.volcengine.vertcdemo.feedshare.feature.effect.customview.EffectStickerLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EffectDialog extends BaseDialog implements IEffectItemChangedListener {
    public static final int DEFAULT_PROGRESS = 50;

    public interface AdjustCallBack {
        void updateVideoEffectNode(String path, String key, float val);

        void setVideoEffectColorFilter(String path);

        void updateColorFilterIntensity(float intensity);

        void setStickerNodes(String path);

        void reset();
    }

    private int mDefaultProgress = DEFAULT_PROGRESS;
    private final int FILTER_DEFAULT_PROGRESS = 0;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SeekBar mSeekbar;
    private EffectBeautyLayout mBeautyLayout;
    private EffectFilterLayout mFilterLayout;
    private EffectStickerLayout mStickerLayout;

    public static final String[] TAB_NAMES = {"美颜", "滤镜", "贴纸"};
    private AdjustCallBack mAdjustCallBack;

    public EffectDialog(@NonNull Context context) {
        super(context);
    }

    public void setCallBack(AdjustCallBack callBack) {
        mAdjustCallBack = callBack;
    }

    public void setDefaultProgress(int progress) {
        this.mDefaultProgress = progress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.effect_dialog_layout);
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setDimAmount(0);
        initUI();
        setDefaultProgress();
    }

    private void setDefaultProgress() {
        if (mBeautyLayout == null) return;
        mBeautyLayout.setEffectProgress(R.id.effect_whiten, mDefaultProgress);
        mBeautyLayout.setEffectProgress(R.id.effect_smooth, mDefaultProgress);
        mBeautyLayout.setEffectProgress(R.id.effect_sharp, mDefaultProgress);
        mBeautyLayout.setEffectProgress(R.id.effect_big_eye, mDefaultProgress);
        mBeautyLayout.onClick(mBeautyLayout.findViewById(R.id.effect_whiten));
    }

    public void initUI() {
        mViewPager = findViewById(R.id.effect_vp);
        TabViewPageAdapter adapter = new TabViewPageAdapter(Arrays.asList(TAB_NAMES), generateTabViews());
        mViewPager.setAdapter(adapter);

        mTabLayout = findViewById(R.id.effect_tab);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (TextUtils.equals(tab.getText(), "贴纸")) {
                    mSeekbar.setVisibility(View.GONE);
                } else if (TextUtils.equals(tab.getText(), "美颜")) {
                    mSeekbar.setProgress(mBeautyLayout.getEffectProgress(mBeautyLayout.getSelectedId(), mDefaultProgress));
                    mSeekbar.setVisibility(mBeautyLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
                } else if (TextUtils.equals(tab.getText(), "滤镜")) {
                    mSeekbar.setProgress(mFilterLayout.getEffectProgress(mFilterLayout.getSelectedId(), FILTER_DEFAULT_PROGRESS));
                    mSeekbar.setVisibility(mFilterLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mSeekbar = findViewById(R.id.effect_seekbar);
        mSeekbar.setVisibility(mBeautyLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
        int currentProgress = mBeautyLayout.getEffectProgress(mBeautyLayout.getSelectedId(), this.mDefaultProgress);
        mSeekbar.setProgress(currentProgress);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int viewId = -1;

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (mAdjustCallBack == null) {
                    return;
                }
                float value = seekBar.getProgress() / 100f;
                View currentView = adapter.getPrimaryItem();
                int tabPos = mTabLayout.getSelectedTabPosition();
                if (tabPos == 0) {
                    EffectBeautyLayout effectBeautyLayout = (EffectBeautyLayout) currentView;
                    viewId = effectBeautyLayout.getSelectedId();
                    if (viewId == R.id.effect_whiten) {
                        mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "whiten", value); // 美白
                    } else if (viewId == R.id.effect_smooth) {
                        mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "smooth", value); // 磨皮
                    } else if (viewId == R.id.effect_big_eye) {
                        mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Eye", value); // 大眼
                    } else if (viewId == R.id.effect_sharp) {
                        mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Overall", value); // 瘦脸
                    }
                } else if (tabPos == 1) {
                    viewId = ((EffectFilterLayout) currentView).getSelectedId();
                    if (viewId == R.id.effect_landiao) {
                        mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_47_S5");
                    } else if (viewId == R.id.effect_lengyang) {
                        mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_30_Po8");
                    } else if (viewId == R.id.effect_lianai) {
                        mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_24_Po2");
                    } else if (viewId == R.id.effect_yese) {
                        mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_35_L3");
                    }
                    mAdjustCallBack.updateColorFilterIntensity(viewId == R.id.no_select ? 0 : value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int tabPos = mTabLayout.getSelectedTabPosition();
                if (tabPos == 0) {
                    EffectBeautyLayout.setEffectProgress(viewId, seekBar.getProgress());
                } else if (tabPos == 1) {
                    EffectFilterLayout.getSeekBarProgressMap().clear();
                    EffectFilterLayout.getSeekBarProgressMap().put(viewId, seekBar.getProgress());
                }
            }
        });
    }

    public List<View> generateTabViews() {
        List<View> mViews = new ArrayList<>();
        for (String tabName : TAB_NAMES) {
            switch (tabName) {
                case "美颜":
                    mViews.add(mBeautyLayout = new EffectBeautyLayout(getContext(), this));
                    mBeautyLayout.setAdjustCallBack(mAdjustCallBack);
                    break;
                case "滤镜":
                    mViews.add(mFilterLayout = new EffectFilterLayout(getContext(), this));
                    mFilterLayout.setAdjustCallBack(mAdjustCallBack);
                    break;
                case "贴纸":
                    mViews.add(mStickerLayout = new EffectStickerLayout(getContext(), this));
                    break;
                default:
            }
        }
        return mViews;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onChanged(View newItem, View lastItem) {
        if (newItem.getId() == R.id.no_select) {
            mSeekbar.setVisibility(View.GONE);
        } else if (mTabLayout.getSelectedTabPosition() != 2) {
            mSeekbar.setVisibility(View.VISIBLE);
        }

        if (mTabLayout.getSelectedTabPosition() == 0) {
            int currentProgress = mBeautyLayout.getEffectProgress(newItem.getId(), this.mDefaultProgress);
            mBeautyLayout.updateStatusByValue();
            if (mAdjustCallBack == null) {
                return;
            }
            if (newItem.getId() == R.id.no_select) {
                mSeekbar.setProgress(0);
                mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "whiten", 0);
                mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "smooth", 0);
                mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Eye", 0);
                mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Overall", 0);
            } else {
                mSeekbar.setProgress(currentProgress);
                for (Map.Entry<Integer, Integer> entry : EffectBeautyLayout.getSeekBarProgressMap().entrySet()) {
                    int value = entry.getValue() == null ? 0 : entry.getValue();
                    int id = entry.getKey();
                    updateEffectNode(id, value);
                }
                updateEffectNode(newItem.getId(), mBeautyLayout.getEffectProgress(newItem.getId(), this.mDefaultProgress));
            }
        } else if (mTabLayout.getSelectedTabPosition() == 1) {
            int currentProgress = mFilterLayout.getEffectProgress(newItem.getId(), FILTER_DEFAULT_PROGRESS);
            mSeekbar.setProgress(currentProgress);
            for (Map.Entry<Integer, Integer> entry : EffectFilterLayout.getSeekBarProgressMap().entrySet()) {
                if (entry.getKey() != newItem.getId()) {
                    entry.setValue(0);
                }
            }
            if (newItem.getId() == R.id.effect_landiao) {
                mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_47_S5");
            } else if (newItem.getId() == R.id.effect_lengyang) {
                mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_30_Po8");
            } else if (newItem.getId() == R.id.effect_lianai) {
                mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_24_Po2");
            } else if (newItem.getId() == R.id.effect_yese) {
                mAdjustCallBack.setVideoEffectColorFilter(EffectHelper.getByteColorFilterPath() + "Filter_35_L3");
            }
            mAdjustCallBack.updateColorFilterIntensity((float) currentProgress / 100);
        } else if (mTabLayout.getSelectedTabPosition() == 2) {
            int id = newItem.getId();
            if (id == R.id.effect_shaonvmanhua) {
                mAdjustCallBack.setStickerNodes(EffectStickerLayout.KEY_STICKER_NAME_CARTOON_GIRL);
            } else if (id == R.id.effect_manhuanansheng) {
                mAdjustCallBack.setStickerNodes(EffectStickerLayout.KEY_STICKER_NAME_CARTOON_BOY);
            } else if (id == R.id.effect_suixingshan) {
                mAdjustCallBack.setStickerNodes(EffectStickerLayout.KEY_STICKER_NAME_STAR_BLING);
            } else if (id == R.id.effect_fuguyanjing) {
                mAdjustCallBack.setStickerNodes(EffectStickerLayout.KEY_STICKER_NAME_RETRO_GLASSES);
            } else if (id == R.id.no_select) {
                mAdjustCallBack.setStickerNodes("");
            }
        }
    }

    private void updateEffectNode(int viewID, float value) {
        if (viewID == R.id.effect_whiten) {
            mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "whiten", value / 100);
        } else if (viewID == R.id.effect_smooth) {
            mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteComposePath(), "smooth", value / 100);
        } else if (viewID == R.id.effect_big_eye) {
            mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Eye", value / 100);
        } else if (viewID == R.id.effect_sharp) {
            mAdjustCallBack.updateVideoEffectNode(EffectHelper.getByteShapePath(), "Internal_Deform_Overall", value / 100);
        }
    }
}
