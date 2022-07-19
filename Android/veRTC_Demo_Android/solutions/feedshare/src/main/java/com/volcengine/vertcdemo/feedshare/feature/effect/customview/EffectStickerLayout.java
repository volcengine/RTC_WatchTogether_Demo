package com.volcengine.vertcdemo.feedshare.feature.effect.customview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.feedshare.R;
import com.volcengine.vertcdemo.feedshare.core.FeedShareDataManger;
import com.volcengine.vertcdemo.feedshare.feature.effect.IEffectItemChangedListener;

public class EffectStickerLayout extends LinearLayout implements View.OnClickListener {

    public static final String KEY_STICKER_NAME_CARTOON_BOY = "manhuanansheng";
    //因为卡通少女素材有问题暂时展示的是神仙高光
    public static final String KEY_STICKER_NAME_CARTOON_GIRL = "shenxiangaoguang";
    public static final String KEY_STICKER_NAME_STAR_BLING = "suixingshan";
    public static final String KEY_STICKER_NAME_RETRO_GLASSES = "fuguxiangzuanyanjing";

    private ImageView mNoSelectedBtn;
    private ImageView mNvShengBtn;
    private ImageView mNanShengBtn;
    private ImageView mSuixingshanBtn;
    private ImageView mFuguyanjingBtn;
    private View mSelectedBtn;
    private IEffectItemChangedListener mEffectItemChangedListener;


    public EffectStickerLayout(Context context, IEffectItemChangedListener listener) {
        super(context);
        this.mEffectItemChangedListener = listener;
        initView();
    }

    public EffectStickerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectStickerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.dialog_effect_sticker, this, true);

        mNoSelectedBtn = findViewById(R.id.no_select);
        mNoSelectedBtn.setOnClickListener(this);
        mSelectedBtn = mNoSelectedBtn;
        mNvShengBtn = findViewById(R.id.effect_shaonvmanhua);
        mNvShengBtn.setOnClickListener(this);
        mNanShengBtn = findViewById(R.id.effect_manhuanansheng);
        mNanShengBtn.setOnClickListener(this);
        mSuixingshanBtn = findViewById(R.id.effect_suixingshan);
        mSuixingshanBtn.setOnClickListener(this);
        mFuguyanjingBtn = findViewById(R.id.effect_fuguyanjing);
        mFuguyanjingBtn.setOnClickListener(this);
        setSelectedPath(FeedShareDataManger.getInstance().getEffectHelper().getStickerPath());
    }

    private void setSelectedPath(String path) {
        if (TextUtils.equals(path, KEY_STICKER_NAME_CARTOON_BOY)) {
            updateUI(mNvShengBtn);
        } else if (TextUtils.equals(path, KEY_STICKER_NAME_CARTOON_GIRL)) {
            updateUI(mNanShengBtn);
        } else if (TextUtils.equals(path, KEY_STICKER_NAME_STAR_BLING)) {
            updateUI(mSuixingshanBtn);
        } else if (TextUtils.equals(path, KEY_STICKER_NAME_RETRO_GLASSES)) {
            updateUI(mFuguyanjingBtn);
        } else {
            updateUI(mNoSelectedBtn);
        }
    }

    private void updateUI(View view) {
        if (view.getId() == R.id.no_select) {
            ((ImageView) view).setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            ((ImageView) view).setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        ((ImageView) mSelectedBtn).setBackgroundResource(R.drawable.effect_btn_normal_bg);
        mSelectedBtn = view;
    }

    public int getSelectedId() {
        if (mSelectedBtn != null) {
            return mSelectedBtn.getId();
        }
        return -1;
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectedBtn) {
            return;
        }
        if (v.getId() == R.id.no_select) {
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
}
