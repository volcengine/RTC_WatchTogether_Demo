//
//  FeedShareChatRoomVideoView.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/5.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareRoomVideoView.h"
#import "FeedShareRTCManager.h"

@interface FeedShareRoomVideoView ()

@property (nonatomic, strong) UIView *fullView;
@property (nonatomic, strong) NSArray<UIView *> *viewArray;

@end

@implementation FeedShareRoomVideoView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.fullView];
    [self.fullView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self);
    }];
    
    UIView *firstView = [[UIView alloc] init];
    firstView.tag = 1;
    firstView.hidden = YES;
    [self addSubview:firstView];
    [firstView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self).offset(100);
        make.left.equalTo(self).offset(23);
        make.size.mas_equalTo(CGSizeMake(90, 160));
    }];
    UIView *secondView = [[UIView alloc] init];
    secondView.tag = 2;
    secondView.hidden = YES;
    [self addSubview:secondView];
    [secondView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(firstView);
        make.top.equalTo(firstView.mas_bottom).offset(10);
        make.size.mas_equalTo(CGSizeMake(90, 160));
    }];
    self.viewArray = @[firstView, secondView];
}

- (void)updateVideoViews {
    NSMutableDictionary *dict = [FeedShareRTCManager shareRtc].streamViewDic.mutableCopy;
    NSString *localViewKey = [NSString stringWithFormat:@"self_%@", [LocalUserComponents userModel].uid];
    
    UIView *localVideoView = [dict objectForKey:localViewKey];
    localVideoView.backgroundColor = UIColor.blackColor;
    [_fullView addSubview:localVideoView];
    [localVideoView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(_fullView);
    }];
    [dict removeObjectForKey:localViewKey];
    
    NSArray *userIDs = dict.allKeys;
    
    for (int i = 0; i < 2; i++) {
        UIView *view = self.viewArray[i];
        if (userIDs.count > i) {
            view.hidden = NO;
            UIView *videoView = [dict objectForKey:userIDs[i]];
            [view addSubview:videoView];
            [videoView mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.edges.equalTo(view);
            }];
        }
        else {
            view.hidden = YES;
        }
    }
}

#pragma mark - getter
- (UIView *)fullView {
    if (!_fullView) {
        _fullView = [[UIView alloc] init];
    }
    return _fullView;
}

@end
