// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

public interface IVideoLayerCommand {

    int VIDEO_HOST_CMD_PLAY = 100;
    int VIDEO_HOST_CMD_PAUSE = 101;
    int VIDEO_HOST_CMD_REPLY = 102;
    int VIDEO_HOST_CMD_SEEK = 209;

    int getCommand();

    <T> T getParam(Class<T> clazz);
}
