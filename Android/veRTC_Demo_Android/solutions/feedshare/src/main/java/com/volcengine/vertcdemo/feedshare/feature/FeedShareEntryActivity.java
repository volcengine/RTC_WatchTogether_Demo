package com.volcengine.vertcdemo.feedshare.feature;

import android.os.Bundle;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.feedshare.feature.preview.PreviewActivity;

public class FeedShareEntryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RtmInfo rtmInfo = getIntent() == null ? null : getIntent().getParcelableExtra(RtmInfo.KEY_RTM);
        PreviewActivity.start(FeedShareEntryActivity.this, rtmInfo);
        finish();
    }

    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }
}