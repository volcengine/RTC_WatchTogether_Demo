// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.event;

public class RTCNetStatusEvent {
    public boolean unblocked;

    public RTCNetStatusEvent(boolean unblocked) {
        this.unblocked = unblocked;
    }
}
