//
//  FeedShareChatRoomViewController.m
//  veRTC_Demo
//
//  Created by on 2022/1/5.
//  
//

#import "FeedShareRoomViewController.h"
#import "FeedShareRoomViewController+SocketControl.h"

#import "FeedShareRoomVideoView.h"
#import "FeedShareBottomButtonsView.h"
#import "FeedShareNavView.h"

#import "FeedShareRTCManager.h"
#import "SystemAuthority.h"
#import "FeedShareMessageComponent.h"
#import "FeedShareRTMManager.h"
#import "FeedShareMediaModel.h"
#import "BytedEffectProtocol.h"

@interface FeedShareRoomViewController ()<FeedShareBottomButtonsViewDelegate, FeedShareMessageComponentDelegate>

@property (nonatomic, strong) FeedShareRoomModel *roomModel;

@property (nonatomic, strong) FeedShareRoomVideoView *videoView;
@property (nonatomic, strong) FeedShareNavView *navView;
@property (nonatomic, strong) FeedShareBottomButtonsView *buttonsView;

@property (nonatomic, strong) BytedEffectProtocol *beautyComponent;
@property (nonatomic, strong) FeedShareMessageComponent *messageComponent;

@property (nonatomic, assign) BOOL isActivelyQuit;

@end

@implementation FeedShareRoomViewController

- (void)dealloc {
    if (self.playController) {
        [self.playController destroy];
    }
    [[FeedShareRTCManager shareRtc].streamViewDic removeAllObjects];
    [UIApplication sharedApplication].idleTimerDisabled = NO;
}

- (instancetype)initWithRoomModel:(FeedShareRoomModel *)roomModel {
    if (self = [super init]) {
        self.roomModel = roomModel;
        [[FeedShareRTCManager shareRtc] bingCanvasViewToUid:[LocalUserComponent userModel].uid];
        
        [UIApplication sharedApplication].idleTimerDisabled = YES;
        
        [self addSocketListener];
        
        __weak typeof(self) weakSelf = self;
        [FeedShareRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString * _Nonnull roomId, NSInteger errorCode, NSInteger joinType) {
            if (errorCode == 0 && joinType == 1) {
                // 重新登录
                [weakSelf loadDataWithReconnect];
            }
        };
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor blackColor];
    [self setupViews];
    
    // resume local render effect
    [self.beautyComponent resumeLocalEffect];
    
    if ([self isHost]) {
        [self requestVideoListComplete:nil];
    }
    self.messageComponent = [[FeedShareMessageComponent alloc] initWithDelegate:self];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.buttonsView.enableVideo = [FeedShareMediaModel shared].enableVideo;
    self.buttonsView.enableAudio = [FeedShareMediaModel shared].enableAudio;
    [[FeedShareRTCManager shareRtc] enableLocalAudio:[FeedShareMediaModel shared].enableAudio];
    [[FeedShareRTCManager shareRtc] enableLocalVideo:[FeedShareMediaModel shared].enableVideo];
    
    [self.messageComponent addMessageListener];
    
    __weak typeof(self) weakSelf = self;
    [FeedShareRTCManager shareRtc].roomUsersDidChangeBlock = ^{
        [weakSelf.videoView updateVideoViews];
    };
    
    [[FeedShareRTCManager shareRtc] didChangeNetworkQuality:^(FeedShareNetworkQualityStatus status, NSString * _Nonnull uid) {
        [weakSelf.navView.networkView updateNetworkQualityStstus:status];
    }];
    
    // show local render view
    [self.videoView updateVideoViews];
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
            if (self.beautyComponent) {
                [self.beautyComponent showWithType:EffectBeautyRoleTypeHost fromSuperView:self.view dismissBlock:^(BOOL result) {
                }];
            } else {
                [[ToastComponent shareToastComponent] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
            }
        }
            break;
            
        case FeedShareButtonTypeWatch: {
            if ([self isHost]) {
                [self startFeedShare];
            } else {
                self.buttonsView.isLoading = YES;
                [self.messageComponent sendRequestFeedShareMessage:self.roomModel.hostUid];
            }
        }
            break;
            
        default:
            break;
    }
}

#pragma mark - FeedShareMessageComponentDelegate
- (void)feedShareMessageComponentDidReceiveRequestFeedShare:(FeedShareMessageComponent *)messageComponent {
    if (self.buttonsView.userInteractionEnabled) {
        [self startFeedShare];
    }
}

#pragma mark - Methods
- (void)setupViews {
    [self.view addSubview:self.videoView];
    [self.videoView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];
    [self.view addSubview:self.navView];
    [self.view addSubview:self.buttonsView];
    [self.buttonsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.bottom.mas_equalTo(-10 - [DeviceInforTool getVirtualHomeHeight]);
    }];
}

- (void)requestVideoListComplete:(void(^)(RTMACKModel *model))complete {
    __weak typeof(self) weakSelf = self;
    [FeedShareRTMManager requestVideoListWithRoomID:self.roomModel.roomID block:^(NSArray<FeedShareVideoModel *> * _Nonnull videoList, RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        } else {
            weakSelf.videoModelArray = videoList;
        }
        if (complete) {
            complete(model);
        }
    }];
}

- (void)startFeedShare {
    self.buttonsView.userInteractionEnabled = NO;
    self.buttonsView.isLoading = YES;
    if (self.videoModelArray.count == 0) {
        __weak typeof(self) weakSelf = self;
        [self requestVideoListComplete:^(RTMACKModel *model) {
            if (model.result) {
                [weakSelf requestChangeRoomStatus];
            } else {
                [[ToastComponent shareToastComponent] showWithMessage:model.message];
                weakSelf.buttonsView.userInteractionEnabled = YES;
                weakSelf.buttonsView.isLoading = NO;
            }
        }];
    } else {
        [self requestChangeRoomStatus];
    }
    
}

- (void)requestChangeRoomStatus {
    
    __weak typeof(self) weakSelf = self;
    [FeedShareRTMManager requestChangeRoomScene:FeedShareRoomStatusShare roomID:self.roomModel.roomID block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        } else {
            weakSelf.roomModel.roomStatus = FeedShareRoomStatusShare;
            [weakSelf pushPlayViewController];
        }
        weakSelf.buttonsView.userInteractionEnabled = YES;
        weakSelf.buttonsView.isLoading = NO;
    }];
}

- (BOOL)isHost {
    return [self.roomModel.hostUid isEqual:[LocalUserComponent userModel].uid];
}

- (void)receivedVideoList:(NSArray<FeedShareVideoModel *> *)videoList {
    if ([self isHost]) {
        return;
    }
    
    if (self.playController) {
        [self.playController updateVideoList:videoList];
    }
    else {
        self.videoModelArray = videoList;
    }
}

- (void)receiveUpdateRoomScene:(NSString *)roomID
                         scene:(FeedShareRoomStatus)scene {
    if ([self isHost]) {
        return;
    }
    
    NSLog(@"receiveUpdateRoomScene");
    self.roomModel.roomStatus = scene;
    if (scene == FeedShareRoomStatusChat) {
        // Pop
        if (self.playController) {
            [self.playController popToRoomViewController];
            self.playController = nil;
        }
    } else if (scene == FeedShareRoomStatusShare) {
        // Push
        if (self.videoModelArray.count > 0) {
            [self pushPlayViewController];
        }
        self.buttonsView.isLoading = NO;
    } else {
        // error
    }
}

- (void)pushPlayViewController {
    FeedSharePlayViewController *ctrl = [[FeedSharePlayViewController alloc] initWithRoomModel:self.roomModel];
    [ctrl updateVideoList:self.videoModelArray];
    self.playController = ctrl;
    [self.navigationController pushViewController:ctrl animated:YES];
    self.videoModelArray = nil;
}

- (void)receiveFinishRoom:(NSString *)roomID {
    if (self.isActivelyQuit) {
        return;
    }
    if (self.playController) {
        [self.playController popToCreateRoomViewController];
    } else {
        [self quitRoom];
    }
    
    if (![self isHost]) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [[ToastComponent shareToastComponent] showWithMessage:@"房主已关闭房间"];
        });
    }
}

- (void)loadDataWithReconnect {
    __weak __typeof(self) wself = self;
    [FeedShareRTMManager reconnect:^(RTMACKModel * _Nonnull model) {
        if (model.code == RTMStatusCodeUserIsInactive ||
            model.code == RTMStatusCodeRoomDisbanded ||
            model.code == RTMStatusCodeUserNotFound) {
            if (wself.playController) {
                [wself.playController popToCreateRoomViewController];
            } else {
                [wself quitRoom];
            }
            [[ToastComponent shareToastComponent] showWithMessage:model.message delay:0.8];
        }
    }];
}

#pragma mark - actions
- (void)leaveButtonClick {
    self.isActivelyQuit = YES;
    [FeedShareRTMManager requestLeaveRoom:self.roomModel.roomID block:^(RTMACKModel * _Nonnull model) {
        if (!model.result) {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
    }];
    [self quitRoom];
}

- (void)quitRoom {
    [self.navigationController popViewControllerAnimated:YES];
    [[FeedShareRTCManager shareRtc] leaveChannel];
}

#pragma mark - getter

- (FeedShareRoomVideoView *)videoView {
    if (!_videoView) {
        _videoView = [[FeedShareRoomVideoView alloc] init];
    }
    return _videoView;
}



- (BytedEffectProtocol *)beautyComponent {
    if (!_beautyComponent) {
        _beautyComponent = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[FeedShareRTCManager shareRtc].rtcEngineKit];
    }
    return _beautyComponent;
}

- (FeedShareBottomButtonsView *)buttonsView {
    if (!_buttonsView) {
        _buttonsView = [[FeedShareBottomButtonsView alloc] init];
        _buttonsView.type = FeedShareButtonViewTypeRoom;
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
            [weakSelf leaveButtonClick];
        };
    }
    return _navView;
}


@end
