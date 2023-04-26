// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.event;

public class RTCUserJoinEvent {
    public String userId;

    public RTCUserJoinEvent(String userId) {
        this.userId = userId;
    }
}
