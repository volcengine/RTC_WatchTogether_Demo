// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedShareVideoModel.h"
#import "FeedShareToolComponent.h"

@implementation FeedShareVideoModel

+ (NSDictionary *)modelCustomPropertyMapper {
    return @{
        @"videoId" : @"VideoId",
        @"videoUrl" : @"VideoUrl",
        @"coverURL" : @"VideoCoverUrl",
        @"videoDuration" : @"VideoDuration",
        @"videoTitle" : @"VideoTitle",
    };
}

+ (TTVideoEngineUrlSource *)videoEngineUrlSource:(FeedShareVideoModel *)videoModel {
    TTVideoEngineUrlSource *source = [[TTVideoEngineUrlSource alloc] initWithUrl:videoModel.videoUrl cacheKey:[FeedShareToolComponent MD5ForLower32Bate:videoModel.videoUrl] videoId:videoModel.videoId];
    source.title = videoModel.videoTitle;
    source.cover = videoModel.coverURL;
    return source;
}

@end
