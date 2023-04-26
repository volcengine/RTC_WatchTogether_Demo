// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.feature.feedshare;

import com.volcengine.vertcdemo.feedshare.bean.MessageContent;

public interface ISyncHandler {

    /**
     * 处理视频播放状态信息,包含视频的进度信息
     *
     * @param peerUid 发送者userId
     * @param content
     */
    void handleVideoStatus(String peerUid, MessageContent content);

    /**
     * 处理观众请求视频分享
     *
     * @param peerUid 发送者userId
     */
    void handleRequestFeedShare(String peerUid);
}
