// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.view.layer;

public class CommonLayerCommand implements IVideoLayerCommand {

    private final int command;
    private Object params;

    public CommonLayerCommand(int command) {
        this.command = command;
    }

    public CommonLayerCommand(int command, Object params) {
        this.params = params;
        this.command = command;
    }

    public int getCommand() {
        return command;
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
