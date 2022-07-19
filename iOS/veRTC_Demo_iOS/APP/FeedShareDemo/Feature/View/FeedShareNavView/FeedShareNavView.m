//
//  FeedShareNavView.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedShareNavView.h"

#import "FeedShareRTCManager.h"

@interface FeedShareNavView()

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *cameraButton;
@property (nonatomic, strong) UIButton *leaveButton;

@end

@implementation FeedShareNavView

+ (CGFloat)viewHeight {
    return [DeviceInforTool getStatusBarHight] + 44;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:CGRectMake(0, 0, UIScreen.mainScreen.bounds.size.width, [FeedShareNavView viewHeight])]) {
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.titleLabel];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self);
        make.centerX.equalTo(self);
        make.height.mas_equalTo(44);
    }];
    [self addSubview:self.cameraButton];
    [self.cameraButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self).offset(23);
        make.centerY.equalTo(self.titleLabel);
    }];
    [self addSubview:self.leaveButton];
    [self.leaveButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self).offset(-23);
        make.centerY.equalTo(self.titleLabel);
    }];
    [self addSubview:self.networkView];
    [self.networkView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(self.titleLabel);
        make.left.equalTo(self.titleLabel.mas_right).offset(10);
    }];
}

- (void)setTitle:(NSString *)title {
    _title = title;
    _titleLabel.text = title;
}

#pragma mark - actions

- (void)cameraButtonClick {
    [[FeedShareRTCManager shareRtc] switchCamera];
}

- (void)leaveButtonClick {
    if (self.leaveButtonTouchBlock) {
        self.leaveButtonTouchBlock();
    }
}

#pragma mark - getter
- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.font = [UIFont systemFontOfSize:16];
        _titleLabel.textColor = UIColor.blackColor;
    }
    return _titleLabel;
}

- (UIButton *)cameraButton {
    if (!_cameraButton) {
        _cameraButton = [[UIButton alloc] init];
        [_cameraButton setImage:[UIImage imageNamed:@"feed_share_room_camera" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_cameraButton addTarget:self action:@selector(cameraButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _cameraButton;
}

- (UIButton *)leaveButton {
    if (!_leaveButton) {
        _leaveButton = [[UIButton alloc] init];
        [_leaveButton setImage:[UIImage imageNamed:@"feed_share_room_hangeup" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_leaveButton addTarget:self action:@selector(leaveButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _leaveButton;
}

- (FeedShareNetworkView *)networkView {
    if (!_networkView) {
        _networkView = [[FeedShareNetworkView alloc] init];
    }
    return _networkView;
}

@end
