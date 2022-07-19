//
//  FeedSharePlayViewController.m
//  veRTC_Demo
//
//  Created by bytedance on 2022/1/6.
//  Copyright © 2022 bytedance. All rights reserved.
//

#import "FeedSharePlayViewController.h"
#import "FeedShareNavView.h"
#import "FeedShareBottomButtonsView.h"
#import "FeedSharePlaySessionView.h"
#import "FeedSharePlayVolumeView.h"

#import "FeedShareRoomModel.h"
#import "FeedShareRTCManager.h"
#import "SystemAuthority.h"
#import "FeedSharePlayCompoments.h"
#import "FeedShareMessageComponent.h"
#import "FeedShareMediaModel.h"
#import "FeedShareRTMManager.h"
#import <TTSDK/TTVideoEngine+Strategy.h>
#import "BytedEffectProtocol.h"

@interface FeedSharePlayViewController ()<FeedShareBottomButtonsViewDelegate>

@property (nonatomic, strong) FeedShareRoomModel *roomModel;

@property (nonatomic, strong) FeedSharePlaySessionView *sessionView;
@property (nonatomic, strong) FeedShareNavView *navView;
@property (nonatomic, strong) FeedShareBottomButtonsView *buttonsView;
@property (nonatomic, strong) FeedSharePlayVolumeView *volumeView;

@property (nonatomic, strong) BytedEffectProtocol *beautyCompoments;
@property (nonatomic, strong) FeedSharePlayCompoments *playCompoments;

@end

@implementation FeedSharePlayViewController

- (void)dealloc {
    NSLog(@"%s", __func__);
    [[FeedShareRTCManager shareRtc] stopAudioMixing];
}

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel {
    if (self = [super init]) {
        self.roomModel = roomModel;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor grayColor];
    [self setupViews];
    
    [self enableVideoEngineStategy];
    
    // resume local render effect
    [self.beautyCompoments resumeLocalEffect];
    
    [[FeedShareRTCManager shareRtc] startAudioMixing];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = YES;
    
    self.buttonsView.enableVideo = [FeedShareMediaModel shared].enableVideo;
    self.buttonsView.enableAudio = [FeedShareMediaModel shared].enableAudio;
    [[FeedShareRTCManager shareRtc] enableLocalAudio:[FeedShareMediaModel shared].enableAudio];
    [[FeedShareRTCManager shareRtc] enableLocalVideo:[FeedShareMediaModel shared].enableVideo];
    
    __weak typeof(self) weakSelf = self;
    [FeedShareRTCManager shareRtc].roomUsersDidChangeBlock = ^{
        [weakSelf.sessionView updateVideoViews];
    };
    [[FeedShareRTCManager shareRtc] didChangeNetworkQuality:^(FeedShareNetworkQualityStatus status, NSString * _Nonnull uid) {
        [weakSelf.navView.networkView updateNetworkQualityStstus:status];
    }];
    
    
    // show local render view
    [self.sessionView updateVideoViews];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [FeedShareMediaModel shared].enableVideo = self.buttonsView.enableVideo;
    [FeedShareMediaModel shared].enableAudio = self.buttonsView.enableAudio;
}

#pragma mark - FeedShareBottomButtonsViewDelegate
- (void)feedShareBottomButtonsView:(FeedShareBottomButtonsView *)view didClickButtonType:(FeedShareButtonType)type {
    switch (type) {
        case FeedShareButtonTypeAudio: {
            if (![FeedShareMediaModel shared].audioPermissionDenied) {
                [[FeedShareRTCManager shareRtc] enableLocalAudio:view.enableAudio];
            } else {
                AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
                alertCancelModel.title = @"取消";
                AlertActionModel *alertModel = [[AlertActionModel alloc] init];
                alertModel.title = @"确定";
                alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
                    if ([action.title isEqualToString:@"确定"]) {
                        [SystemAuthority autoJumpWithAuthorizationStatusWithType:AuthorizationTypeAudio];
                    }
                };
                [[AlertActionManager shareAlertActionManager] showWithMessage:@"麦克风权限已关闭，请至设备设置页开启" actions:@[alertCancelModel, alertModel]];
            }
        }
            break;
            
        case FeedShareButtonTypeVideo: {
            if (![FeedShareMediaModel shared].videoPermissionDenied) {
                BOOL isEnableVideo = view.enableVideo;
                [[FeedShareRTCManager shareRtc] enableLocalVideo:isEnableVideo];
            } else {
                AlertActionModel *alertCancelModel = [[AlertActionModel alloc] init];
                alertCancelModel.title = @"取消";
                AlertActionModel *alertModel = [[AlertActionModel alloc] init];
                alertModel.title = @"确定";
                alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
                    if ([action.title isEqualToString:@"确定"]) {
                        [SystemAuthority autoJumpWithAuthorizationStatusWithType:AuthorizationTypeCamera];
                    }
                };
                [[AlertActionManager shareAlertActionManager] showWithMessage:@"摄像头权限已关闭，请至设备设置页开启" actions:@[alertCancelModel, alertModel]];
            }
        }
            break;
            
        case FeedShareButtonTypeBeauty: {
            if (self.beautyCompoments) {
                [self.beautyCompoments showWithType:EffectBeautyRoleTypeHost fromSuperView:self.view dismissBlock:^(BOOL result) {
                    
                }];
            } else {
                [[ToastComponents shareToastComponents] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
            }
        }
            break;
            
        case FeedShareButtonTypeSetting: {
            [self.volumeView showinView:self.view];
        }
            break;
            
        default:
            break;
    }
}

#pragma mark - Engine Strategy
- (void)enableVideoEngineStategy {
    [TTVideoEngine enableEngineStrategy:TTVideoEngineStrategyTypeCommon scene:TTVEngineStrategySceneSmallVideo];
    
    [TTVideoEngine enableEngineStrategy:TTVideoEngineStrategyTypePreload scene:TTVEngineStrategySceneSmallVideo];
}

- (void)clearAllEngineStrategy {
    [TTVideoEngine clearAllEngineStrategy];
}


#pragma mark - Methods
- (void)setupViews {
    
    [self.view addSubview:self.playCompoments.tableView];
    [self.playCompoments.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];

    [self.view addSubview:self.navView];
    [self.view addSubview:self.sessionView];
    [self.sessionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view);
        make.top.equalTo(self.navView.mas_bottom);
    }];
    [self.view addSubview:self.buttonsView];
    [self.buttonsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.bottom.mas_equalTo(-10 - [DeviceInforTool getVirtualHomeHeight]);
    }];
}

- (void)updateRoomState {
    [FeedShareMediaModel shared].enableAudio = self.buttonsView.enableAudio;
    [FeedShareMediaModel shared].enableVideo = self.buttonsView.enableVideo;
}

- (void)updateVideoList:(NSArray<FeedShareVideoModel *> *)videoList {
    [self.playCompoments updateVideoList:videoList];
}

- (BOOL)isHost {
    return [self.roomModel.hostUid isEqualToString:[LocalUserComponents userModel].uid];
}

#pragma mark - actions
- (void)quitButtonClick {
    
    [self.playCompoments destroy];
    
    [self updateRoomState];
    
    if ([self isHost]) {
        [FeedShareRTMManager requestChangeRoomScene:FeedShareRoomStatusChat roomID:self.roomModel.roomID block:^(RTMACKModel * _Nonnull model) {
            if (!model.result) {
                [[ToastComponents shareToastComponents] showWithMessage:model.message];
            }
        }];
        [self clearAllEngineStrategy];
        self.roomModel.roomStatus = FeedShareRoomStatusChat;
        [self.navigationController popViewControllerAnimated:YES];
    }
    else {
        [FeedShareRTMManager requestLeaveRoom:self.roomModel.roomID block:^(RTMACKModel * _Nonnull model) {
            if (!model.result) {
                [[ToastComponents shareToastComponents] showWithMessage:model.message];
            }
        }];
        [self clearAllEngineStrategy];
        [self popToCreateRoomViewController];
    }
}

- (void)popToRoomViewController {
    [self.playCompoments destroy];
    
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)popToCreateRoomViewController {
    [self.playCompoments destroy];
    [[FeedShareRTCManager shareRtc] leaveChannel];
    
    UIViewController *jumpVC = nil;
    for (UIViewController *vc in self.navigationController.viewControllers) {
        if ([NSStringFromClass([vc class]) isEqualToString:@"FeedShareCreateRoomViewController"]) {
            jumpVC = vc;
            break;
        }
    }
    if (jumpVC) {
        [self.navigationController popToViewController:jumpVC animated:YES];
    } else {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark - getter

- (BytedEffectProtocol *)beautyCompoments {
    if (!_beautyCompoments) {
        _beautyCompoments = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[FeedShareRTCManager shareRtc].rtcEngineKit];
    }
    return _beautyCompoments;
}

- (FeedShareBottomButtonsView *)buttonsView {
    if (!_buttonsView) {
        _buttonsView = [[FeedShareBottomButtonsView alloc] init];
        _buttonsView.type = FeedShareButtonViewTypeWatch;
        _buttonsView.delegate = self;
    }
    return _buttonsView;
}

- (FeedShareNavView *)navView {
    if (!_navView) {
        _navView = [[FeedShareNavView alloc] init];
        _navView.title = [NSString stringWithFormat:@"房间ID : %@", _roomModel.roomID];
        __weak typeof(self) weakSelf = self;
        _navView.leaveButtonTouchBlock = ^{
            [weakSelf quitButtonClick];
        };
    }
    return _navView;
}

- (FeedSharePlaySessionView *)sessionView {
    if (!_sessionView) {
        _sessionView = [[FeedSharePlaySessionView alloc] init];
    }
    return _sessionView;
}

- (FeedSharePlayCompoments *)playCompoments {
    if (!_playCompoments) {
        _playCompoments = [[FeedSharePlayCompoments alloc] initWithRoomModel:self.roomModel];
    }
    return _playCompoments;
}

- (FeedSharePlayVolumeView *)volumeView {
    if (!_volumeView) {
        _volumeView = [[FeedSharePlayVolumeView alloc] init];
    }
    return _volumeView;
}


@end
