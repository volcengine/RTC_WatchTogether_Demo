// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.feedshare.feature;

import com.google.gson.annotations.SerializedName;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.feedshare.core.Constants;

public class FeedShareJoinRTSRequest extends JoinRTSRequest {
    @SerializedName("content_partner")
    public final String contentPartner;
    @SerializedName("content_category")
    public final String contentCategory;

    public FeedShareJoinRTSRequest() {
        super(Constants.SOLUTION_NAME_ABBR, SolutionDataManager.ins().getToken());

        this.contentPartner = Constants.FEED_SHARE_CONTENT_PARTNER;
        this.contentCategory = Constants.FEED_SHARE_CONTENT_CATEGORY;
    }


    @Override
    public String toString() {
        return "FeedShareJoinRTSRequest{" +
                "scenesName='" + scenesName + '\'' +
                ", loginToken='" + loginToken + '\'' +
                ", contentPartner='" + contentPartner + '\'' +
                ", contentCategory='" + contentCategory + '\'' +
                '}';
    }
}
