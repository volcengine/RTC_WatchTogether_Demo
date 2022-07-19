//
//  FeedShareVideoInfo.h
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/7.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <TTSDK/TTVideoEngineVidSource.h>
#import <TTSDK/TTVideoEngineUrlSource.h>
#import <TTSDK/TTVideoEngineMultiEncodingUrlSource.h>
#import "TTVideoEngineUrlSource+FeedShareUrlSource.h"

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareVideoModel : NSObject

@property (nonatomic, copy) NSString *videoId;
@property (nonatomic, copy) NSString *videoUrl;
@property (nonatomic, copy) NSString *coverURL;
@property (nonatomic, copy) NSString *videoDuration;
@property (nonatomic, copy) NSString *videoTitle;

+ (TTVideoEngineUrlSource *)videoEngineUrlSource:(FeedShareVideoModel *)videoModel;

@end

NS_ASSUME_NONNULL_END
