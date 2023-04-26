// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.event;

public class RTCErrorEvent {
    public int errorCode;

    public RTCErrorEvent(int errorCode) {
        this.errorCode = errorCode;
    }
}
