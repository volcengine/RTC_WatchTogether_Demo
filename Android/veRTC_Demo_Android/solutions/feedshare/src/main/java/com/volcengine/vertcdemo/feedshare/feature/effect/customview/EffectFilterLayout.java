package com.volcengine.vertcdemo.feedshare.feature.effect.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.feature.effect.EffectDialog;
import com.volcengine.vertcdemo.feedshare.feature.effect.IEffectItemChangedListener;

import java.util.HashMap;

public class EffectFilterLayout extends LinearLayout implements View.OnClickListener {
    private ImageView mNoSelectedBtn;
    private ImageView mLandiaoBtn;
    private ImageView mLengyangBtn;
    private ImageView mlianaiBtn;
    private ImageView mYeseBtn;

    private View mSelectedBtn;
    private IEffectItemChangedListener mEffectItemChangedListener;
    private EffectDialog.AdjustCallBack mAdjustCallBack;

    private static final HashMap<Integer, Integer> mSeekBarProgressMap = new HashMap<>();

    public EffectFilterLayout(Context context, IEffectItemChangedListener listener) {
        super(context);
        this.mEffectItemChangedListener = listener;
        initView();
    }

    public EffectFilterLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectFilterLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setAdjustCallBack(EffectDialog.AdjustCallBack callBack) {
        mAdjustCallBack = callBack;
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.dialog_effect_filter, this, true);

        mNoSelectedBtn = findViewById(R.id.no_select);
        mNoSelectedBtn.setOnClickListener(this);
        mSelectedBtn = mNoSelectedBtn;
        mLandiaoBtn = findViewById(R.id.effect_landiao);
        mLandiaoBtn.setOnClickListener(this);
        mLengyangBtn = findViewById(R.id.effect_lengyang);
        mLengyangBtn.setOnClickListener(this);
        mlianaiBtn = findViewById(R.id.effect_lianai);
        mlianaiBtn.setOnClickListener(this);
        mYeseBtn = findViewById(R.id.effect_yese);
        mYeseBtn.setOnClickListener(this);

        if (mLastId == R.id.effect_landiao) {
            updateUI(mLandiaoBtn);
        } else if (mLastId == R.id.effect_lengyang) {
            updateUI(mLengyangBtn);
        } else if (mLastId == R.id.effect_lianai) {
            updateUI(mlianaiBtn);
        } else if (mLastId == R.id.effect_yese) {
            updateUI(mYeseBtn);
        } else {
            updateUI(mNoSelectedBtn);
        }
    }

    public int getSelectedId() {
        return mLastId;
    }

    private void updateUI(View view) {
        if (view.getId() == R.id.no_select) {
            ((ImageView) view).setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            ((ImageView) view).setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        if (view != mSelectedBtn) {
            ((ImageView) mSelectedBtn).setBackgroundResource(R.drawable.effect_btn_normal_bg);
        }
        mSelectedBtn = view;
    }

    private static int mLastId = R.id.no_select;

    @Override
    public void onClick(View v) {
        mLastId = v.getId();
        if (v == mSelectedBtn) {
            return;
        }
        if (v.getId() == R.id.no_select) {
            resetEffect();
            ((ImageView) v).setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            ((ImageView) v).setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        ((ImageView) mSelectedBtn).setBackgroundResource(R.drawable.effect_btn_normal_bg);
        if (mEffectItemChangedListener != null) {
            mEffectItemChangedListener.onChanged(v, mSelectedBtn);
        }

        mSelectedBtn = v;
    }

    public int getEffectProgress(@IdRes int id, int defValue) {
        if (mSeekBarProgressMap.containsKey(id)) {
            return mSeekBarProgressMap.get(id);
        }
        return defValue;
    }

    public static void setEffectProgress(@IdRes int id, int value) {
        mSeekBarProgressMap.put(id, value);
    }

    public static HashMap<Integer, Integer> getSeekBarProgressMap() {
        return mSeekBarProgressMap;
    }

    public void resetEffect() {
        if (mAdjustCallBack != null) {
            mAdjustCallBack.updateColorFilterIntensity(0);
        }
        for (int key : mSeekBarProgressMap.keySet()) {
            mSeekBarProgressMap.put(key, 0);
        }
    }

}
