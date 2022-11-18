//
//  FeedShareVideoInfo.h
//  veRTC_Demo
//
//  Created by on 2022/1/7.
//  
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
