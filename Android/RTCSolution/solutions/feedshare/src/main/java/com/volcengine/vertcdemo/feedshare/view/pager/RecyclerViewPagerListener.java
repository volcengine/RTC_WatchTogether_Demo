// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.pager;

import android.view.View;

public interface RecyclerViewPagerListener {
    void onInitComplete(int position, View view);

    void onPageRelease(int position, View view);

    void onPageSelected(int position, View view);
}
