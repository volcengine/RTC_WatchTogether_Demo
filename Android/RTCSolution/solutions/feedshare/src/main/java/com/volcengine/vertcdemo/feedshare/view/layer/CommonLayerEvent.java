// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

public class CommonLayerEvent implements IVideoLayerEvent {

    private Object params;
    private final int type;

    public CommonLayerEvent(int type) {
        this.type = type;
    }

    public CommonLayerEvent(int type, Object params) {
        this.type = type;
        this.params = params;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object getParam() {
        return params;
    }

    @Override
    public <T> T getParam(Class<T> clazz) {
        if (clazz != null && clazz.isInstance(params)) {
            // noinspection unchecked
            return (T) params;
        }
        return null;
    }

    public void setParams(Object params) {
        this.params = params;
    }
}
