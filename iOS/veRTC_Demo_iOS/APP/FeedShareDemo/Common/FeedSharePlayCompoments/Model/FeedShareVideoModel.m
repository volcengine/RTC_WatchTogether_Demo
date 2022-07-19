//
//  FeedShareVideoInfo.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/7.
//  Copyright © 2022 bytedance. All rights reserved.
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
