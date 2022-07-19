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
import java.util.Map;

public class EffectBeautyLayout extends LinearLayout implements View.OnClickListener {

    private ImageView mNoSelectedBtn;
    private ImageView mWhitenBtn;
    private ImageView mSmoothBtn;
    private ImageView mSharpBtn;
    private ImageView mBigEyeBtn;

    private View mSelectedBtn;
    private IEffectItemChangedListener mEffectItemChangedListener;
    private EffectDialog.AdjustCallBack mAdjustCallBack;


    private static final HashMap<Integer, Integer> mSeekBarProgressMap = new HashMap<>();

    public EffectBeautyLayout(Context context, IEffectItemChangedListener listener) {
        super(context);
        this.mEffectItemChangedListener = listener;
        initView();
    }

    public void setAdjustCallBack(EffectDialog.AdjustCallBack callBack){
        mAdjustCallBack = callBack;
    }

    public EffectBeautyLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectBeautyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.dialog_effect_beauty, this, true);

        mNoSelectedBtn = findViewById(R.id.no_select);
        mNoSelectedBtn.setOnClickListener(this);
        mSelectedBtn = mNoSelectedBtn;
        mWhitenBtn = findViewById(R.id.effect_whiten);
        mWhitenBtn.setOnClickListener(this);
        mSmoothBtn = findViewById(R.id.effect_smooth);
        mSmoothBtn.setOnClickListener(this);
        mSharpBtn = findViewById(R.id.effect_sharp);
        mSharpBtn.setOnClickListener(this);
        mBigEyeBtn = findViewById(R.id.effect_big_eye);
        mBigEyeBtn.setOnClickListener(this);

        if (mLastId == R.id.effect_whiten) {
            updateUI(mWhitenBtn);
        } else if (mLastId == R.id.effect_smooth) {
            updateUI(mSmoothBtn);
        } else if (mLastId == R.id.effect_sharp) {
            updateUI(mSharpBtn);
        } else if (mLastId == R.id.effect_big_eye) {
            updateUI(mBigEyeBtn);
        } else {
            updateUI(mNoSelectedBtn);
        }
        updateStatusByValue();
    }

    private void updateUI(View view) {
        ((ImageView) view).setBackgroundResource(R.drawable.effect_btn_selected_bg);
        if (view != mSelectedBtn) {
            ((ImageView) mSelectedBtn).setBackgroundResource(R.drawable.effect_btn_normal_bg);
        }
        mSelectedBtn = view;
    }

    public void updateStatusByValue() {
        for (Map.Entry<Integer, Integer> idValue : mSeekBarProgressMap.entrySet()) {
            ImageView view;
            int id = idValue.getKey();
            int value = idValue.getValue() == null ? 0 : idValue.getValue();
            if (id == R.id.effect_whiten) {
                view = mWhitenBtn;
            } else if (id == R.id.effect_smooth) {
                view = mSmoothBtn;
            } else if (id == R.id.effect_sharp) {
                view = mSharpBtn;
            } else if (id == R.id.effect_big_eye) {
                view = mBigEyeBtn;
            } else {
                view = mNoSelectedBtn;
            }
            if (value > 0) {
                view.setColorFilter(getContext().getResources().getColor(R.color.blue));
            } else {
                view.setColorFilter(getContext().getResources().getColor(R.color.white));
            }
        }
    }

    private static int mLastId = R.id.no_select;

    @Override
    public void onClick(View v) {
        mLastId = v.getId();

        if (v == mSelectedBtn) {
            return;
        }
        if (v == mNoSelectedBtn) {
            resetEffect();
        }
        ((ImageView) v).setBackgroundResource(R.drawable.effect_btn_selected_bg);
        ((ImageView) mSelectedBtn).setBackgroundResource(R.drawable.effect_btn_normal_bg);
        if (mEffectItemChangedListener != null) {
            mEffectItemChangedListener.onChanged(v, mSelectedBtn);
        }
        mSelectedBtn = v;
    }

    public int getSelectedId() {
        return mLastId;
    }


    public int getEffectProgress(@IdRes int id, int defValue) {
        if (mSeekBarProgressMap.containsKey(id)) {
            return mSeekBarProgressMap.get(id);
        }
        return defValue;
    }

    public static HashMap<Integer, Integer> getSeekBarProgressMap() {
        return mSeekBarProgressMap;
    }

    public static void setEffectProgress(@IdRes int id, int value) {
        mSeekBarProgressMap.put(id, value);
    }

    public void resetEffect() {
        if (mAdjustCallBack != null) {
            mAdjustCallBack.updateColorFilterIntensity(0);
            mAdjustCallBack.reset();
        }
        for (int key : mSeekBarProgressMap.keySet()) {
            mSeekBarProgressMap.put(key, 0);
            ((ImageView) findViewById(key)).setColorFilter(getContext().getResources().getColor(R.color.white));
        }
    }
}
