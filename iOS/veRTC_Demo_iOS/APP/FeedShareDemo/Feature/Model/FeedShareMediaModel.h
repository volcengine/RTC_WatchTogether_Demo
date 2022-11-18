//
//  FeedShareMediaModel.h
//  veRTC_Demo
//
//  Created by on 2022/4/7.
//  
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FeedShareMediaModel : NSObject

@property (nonatomic, assign) BOOL enableAudio;
@property (nonatomic, assign) BOOL enableVideo;
@property (nonatomic, assign) BOOL audioPermissionDenied;
@property (nonatomic, assign) BOOL videoPermissionDenied;

+ (instancetype)shared;

- (void)resetMediaStatus;

@end

NS_ASSUME_NONNULL_END
