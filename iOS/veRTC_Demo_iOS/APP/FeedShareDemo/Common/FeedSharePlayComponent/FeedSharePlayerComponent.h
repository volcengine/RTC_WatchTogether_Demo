//
//  FeedSharePlayerComponent.h
//  veRTC_Demo
//
//  Created by on 2022/1/7.
//  
//

#import <Foundation/Foundation.h>
#import "FeedShareVideoModel.h"

typedef NS_ENUM(NSInteger, TWPlaybackState) {
    TWPlaybackStateStopped,
    TWPlaybackStatePlaying,
    TWPlaybackStatePaused,
    TWPlaybackStateError,
};

NS_ASSUME_NONNULL_BEGIN


@interface FeedSharePlayerComponent : NSObject

@property (nonatomic, strong) FeedShareVideoModel *videoModel;

@property (nonatomic, assign, readonly) NSTimeInterval totalDuration;
@property (nonatomic, assign, readonly) NSTimeInterval currentDuration;
@property (nonatomic, assign, readonly) BOOL isPlaying;
@property (nonatomic, assign, readonly) BOOL isPaused;

- (instancetype)initWithPlayerView:(UIView *)videoView;
- (void)play;
- (void)pause;
- (void)stop;
- (void)setCurrentPlaybackTime:(NSTimeInterval)time complete:(void(^)(void))complete;



@end

NS_ASSUME_NONNULL_END
