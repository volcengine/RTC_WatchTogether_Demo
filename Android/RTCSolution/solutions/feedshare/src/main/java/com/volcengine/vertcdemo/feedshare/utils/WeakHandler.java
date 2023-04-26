// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakHandler extends Handler {

    public interface IHandler {
        void handleMsg(Message msg);
    }

    WeakReference<IHandler> mRef;

    public WeakHandler(IHandler handler) {
        this(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper(), handler);
    }

    public WeakHandler(Looper looper, IHandler handler) {
        super(looper);
        mRef = new WeakReference<>(handler);
    }

    @Override
    public void handleMessage(Message msg) {
        IHandler handler = mRef.get();
        if (handler != null && msg != null)
            handler.handleMsg(msg);
    }
}