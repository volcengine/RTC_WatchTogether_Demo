// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "FeedShareCreateRoomViewController.h"
#import "FeedShareRoomViewController.h"

#import "FeedShareCreateRoomButtonsView.h"
#import "FeedShareCreateRoomTipView.h"

#import "FeedShareRTCManager.h"
#import "FeedShareRoomModel.h"
#import "FeedShareMediaModel.h"
#import "FeedShareRTSManager.h"
#import <TTSDK/TTSDKManager.h>
#import <TTSDK/TTVideoEngineHeader.h>
#import "BytedEffectProtocol.h"

#define TEXTFIELD_MAX_LENGTH 18

@interface FeedShareCreateRoomViewController ()
<
UITextFieldDelegate,
FeedShareCreateRoomButtonsViewDelegate
>

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UIButton *closeButton;
@property (nonatomic, strong) UIButton *enterRoomBtn;
@property (nonatomic, strong) UIView *roomTextView;
@property (nonatomic, strong) UITextField *roomIdTextField;
@property (nonatomic, strong) UIImageView *emptImageView;
@property (nonatomic, strong) UIView *videoView;
@property (nonatomic, strong) FeedShareCreateRoomButtonsView *buttonsView;
@property (nonatomic, strong) FeedShareCreateRoomTipView *tipView;

@property (nonatomic, strong) BytedEffectProtocol *beautyComponent;
@property (nonatomic, strong) UIView *buttonBackView;


@property (nonatomic, strong) UITapGestureRecognizer *tap;

@end

@implementation FeedShareCreateRoomViewController

- (instancetype)init {
    if (self = [super init]) {
        
        
        [[FeedShareMediaModel shared] resetMediaStatus];
        
        /// 第一次进入，使用默认美颜效果
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            [self.beautyComponent reset];
            [self initTTSDK];
        });
        
    }
    return self;
}

- (void)initTTSDK {
    NSString *APPID = TTAPPID;
    NSString *licenseFileName = TTLicenseName;
    NSString *appName = @"vertc";
    NSString *channel = @"App Store";
    NSString *version = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"];
    
    TTSDKConfiguration *configuration = [TTSDKConfiguration defaultConfigurationWithAppID:APPID];
    configuration.appName = appName;
    configuration.channel = channel;
    configuration.bundleID = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"];
    NSString *licenseFilePath = [[NSBundle mainBundle]
                                 pathForResource:licenseFileName
                                 ofType:@"lic"];
    configuration.licenseFilePath = licenseFilePath;
    [TTSDKManager startWithConfiguration:configuration];
    
    NSDictionary *appInfo = @{
            TTVideoEngineAID : APPID, /// 您的APPID
            TTVideoEngineAppName : appName,/// appName
            TTVideoEngineChannel : channel,
            TTVideoEngineServiceVendor : @(TTVideoEngineServiceVendorCN),
            TTVideoEngineAppVersion : version
            };
    [TTVideoEngine configureAppInfo:appInfo];
    [TTVideoEngine startOpenGLESActivity]; // 激活OpenGLES环境,必需
    
    // 1. 配置
    TTVideoEngine.ls_localServerConfigure.maxCacheSize = 300 * 1024 * 1024;// 300M
        NSString *cacheDir = [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:@"com.video.cache"];
    TTVideoEngine.ls_localServerConfigure.cachDirectory = cacheDir;
    //2. 启动
    [TTVideoEngine ls_start];
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromRGBHexString:@"#0D0E12"];

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardDidShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardDidHide:) name:UIKeyboardWillHideNotification object:nil];
    
    [self initUIComponent];
    [self.beautyComponent resume];
    
    [[FeedShareRTCManager shareRtc] enableLocalVideo:YES];
    [[FeedShareRTCManager shareRtc] enableLocalAudio:YES];
    [self loadDataWithClearUser];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleLightContent;
    
    [[FeedShareRTCManager shareRtc] updateCameraID:YES];
    [[FeedShareRTCManager shareRtc] startPreview:self.videoView];
    self.videoView.hidden = ![FeedShareMediaModel shared].enableVideo;
    self.emptImageView.hidden = [FeedShareMediaModel shared].enableVideo;
    
    self.buttonsView.enableVideo = [FeedShareMediaModel shared].enableVideo;
    self.buttonsView.enableAudio = [FeedShareMediaModel shared].enableAudio;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [FeedShareMediaModel shared].enableVideo = self.buttonsView.enableVideo;
    [FeedShareMediaModel shared].enableAudio = self.buttonsView.enableAudio;
}

#pragma mark - 通知
- (void)keyBoardDidShow:(NSNotification *)notifiction {
    CGRect keyboardRect = [[notifiction.userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    [UIView animateWithDuration:0.25 animations:^{
        [self.enterRoomBtn mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self.view).offset(-keyboardRect.size.height - 80/2);
        }];
    }];
    self.emptImageView.hidden = YES;
    [self.view layoutIfNeeded];
}

- (void)keyBoardDidHide:(NSNotification *)notifiction {
    
    [UIView animateWithDuration:0.25 animations:^{
        [self.enterRoomBtn mas_updateConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self.view).offset(-41 - [DeviceInforTool getVirtualHomeHeight]);
        }];
    }];
    self.emptImageView.hidden = self.buttonsView.enableVideo;
    [self.view layoutIfNeeded];
}

#pragma mark - Action Method

- (void)tapGestureAction:(id)sender {
    [self.roomIdTextField resignFirstResponder];
}

- (void)onClickEnterRoom:(UIButton *)sender {
    NSString *roomID = self.roomIdTextField.text;
    
    if (roomID.length <= 0 || ![LocalUserComponent isMatchRoomID:roomID]) {
        return;
    }
    
    roomID = [NSString stringWithFormat:@"feed_%@", self.roomIdTextField.text];
    [PublicParameterComponent share].roomId = roomID;
    [self.view endEditing:YES];
    [[ToastComponent shareToastComponent] showLoading];
    
    __weak typeof(self) weakSelf = self;
    [FeedShareRTSManager requestJoinRoomWithRoomID:roomID block:^(FeedShareRoomModel * _Nonnull roomModel, RTSACKModel * _Nonnull model) {
        
        if (model.result) {
            [weakSelf jumpToRoomViewController:roomModel];
        }
        else if (model.code == 414) {
            [[ToastComponent shareToastComponent] showWithMessage:@"该用户已存在房间中"];
        }
        else if (model.code == 507) {
            [[ToastComponent shareToastComponent] showWithMessage:@"房间人数已满"];
        }
        else {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }

        [[ToastComponent shareToastComponent] dismiss];
    }];
    
}

- (void)jumpToRoomViewController:(FeedShareRoomModel *)roomModel {
    
    [[FeedShareRTCManager shareRtc] joinChannelWithToken:roomModel.rtcToken roomID:roomModel.roomID uid:[LocalUserComponent userModel].uid];
    
    FeedShareRoomViewController *roomViewController = [[FeedShareRoomViewController alloc] initWithRoomModel:roomModel];
    
    if (roomModel.videoList.count > 0 && roomModel.roomStatus == FeedShareRoomStatusShare) {
        FeedSharePlayViewController *playController = [[FeedSharePlayViewController alloc] initWithRoomModel:roomModel];
        [playController updateVideoList:roomModel.videoList];
        roomViewController.playController = playController;
        
        NSMutableArray *viewControllers = self.navigationController.viewControllers.mutableCopy;
        [viewControllers addObjectsFromArray:@[roomViewController, playController]];
        [self.navigationController setViewControllers:viewControllers animated:YES];
    } else {
        roomViewController.videoModelArray = roomModel.videoList;
        [self.navigationController pushViewController:roomViewController animated:YES];
    }
}

#pragma mark - UITextField delegate

- (void)roomNumTextFieldChange:(UITextField *)textField {
    [self updateTextFieldChange:textField];
}

- (void)updateTextFieldChange:(UITextField *)textField {
    NSInteger tagNum = 3001;
    UILabel *label = [self.view viewWithTag:tagNum];
    
    NSString *message = @"";
    BOOL isExceedMaximLength = NO;
    if (textField.text.length > TEXTFIELD_MAX_LENGTH) {
        textField.text = [textField.text substringToIndex:TEXTFIELD_MAX_LENGTH];
        isExceedMaximLength = YES;
    }
    
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(dismissErrorLabel:) object:textField];
    BOOL isIllegal = NO;
    isIllegal = ![LocalUserComponent isMatchRoomID:textField.text];
    if (isIllegal || isExceedMaximLength) {
        if (isIllegal) {
            message = @"请输入数字、英文字母或符号@_-";
        } else if (isExceedMaximLength) {
            [self performSelector:@selector(dismissErrorLabel:) withObject:textField afterDelay:2];
            message = @"输入长度不能超过18位";
        } else {
            message = @"";
        }
        [self updateEnterRoomButtonColor:NO];
    } else {
        BOOL isEnterEnable = self.roomIdTextField.text.length > 0;
        [self updateEnterRoomButtonColor:isEnterEnable];
        message = @"";
    }
    label.text = message;
}

- (void)dismissErrorLabel:(UITextField *)textField {
    NSInteger tagNum = 3001;
    UILabel *label = [self.view viewWithTag:tagNum];
    label.text = @"";
}

#pragma mark - FeedShareCreateRoomButtonsViewDelegate
- (void)feedShareCreateRoomButtonsView:(FeedShareCreateRoomButtonsView *)view didClickButtonType:(FeedShareButtonType)type {
    switch (type) {
        case FeedShareButtonTypeAudio: {
            if (![FeedShareMediaModel shared].audioPermissionDenied) {
                
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
                [FeedShareMediaModel shared].enableVideo = view.enableVideo;
                self.videoView.hidden = !isEnableVideo;
                self.emptImageView.hidden = isEnableVideo;
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
                self.contentView.hidden = YES;
                __weak __typeof(self) wself = self;
                [self.beautyComponent showWithView:self.view dismissBlock:^(BOOL result) {
                    wself.contentView.hidden = NO;
                }];
            } else {
                [[ToastComponent shareToastComponent] showWithMessage:@"开源代码暂不支持美颜相关功能，体验效果请下载Demo"];
            }
        }
            break;
            
        default:
            break;
    }
}

#pragma mark - Private Action

- (void)updateEnterRoomButtonColor:(BOOL)isEnable {
    if (isEnable) {

    } else {

    }
}

- (void)addErrorLabel:(UIView *)view tag:(NSInteger)tag {
    UILabel *label = [[UILabel alloc] init];
    label.tag = tag;
    label.text = @"";
    label.textColor = [UIColor colorFromHexString:@"#F53F3F"];
    label.font = [UIFont systemFontOfSize:14];
    [self.view addSubview:label];
    [label mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(view);
        make.top.mas_equalTo(view.mas_bottom).offset(4);
    }];
}

- (void)initUIComponent {
    [self.view addSubview:self.videoView];
    [self.videoView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];

    [self.view addSubview:self.contentView];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.view);
    }];
    
    [self.contentView addSubview:self.closeButton];
    [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.view).offset(30 + [DeviceInforTool getStatusBarHight]);
        make.right.equalTo(self.view).offset(-17);
    }];
    
    [self.contentView addSubview:self.tipView];
    [self.tipView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.top.equalTo(self.closeButton.mas_bottom).offset(8);
    }];
    
    [self.contentView addSubview:self.emptImageView];
    [self.emptImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.mas_equalTo(120);
        make.centerX.equalTo(self.view);
        make.top.equalTo(self.view).offset(128/2 + [DeviceInforTool getStatusBarHight] + 50);
    }];
    
    [self.contentView addGestureRecognizer:self.tap];
    
    [self.contentView addSubview:self.enterRoomBtn];
    [self.enterRoomBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.size.mas_equalTo(CGSizeMake(227, 50));
        make.centerX.equalTo(self.view);
        make.bottom.equalTo(self.view).offset(-41 - [DeviceInforTool getVirtualHomeHeight]);
    }];
    
    [self.contentView addSubview:self.buttonBackView];
    [self.buttonBackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.contentView);
        make.bottom.equalTo(self.enterRoomBtn.mas_top).offset(-7);
        make.size.mas_equalTo(CGSizeMake(267, 175));
    }];
    
    [self.contentView addSubview:self.buttonsView];
    [self.buttonsView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.contentView);
        make.bottom.equalTo(self.buttonBackView).offset(-20);
    }];
    
    [self.contentView addSubview:self.roomTextView];
    [self.roomTextView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.size.mas_equalTo(CGSizeMake(227, 45));
        make.centerX.equalTo(self.view);
        make.bottom.equalTo(self.buttonsView.mas_top).offset(-32);
    }];
    
    [self addErrorLabel:self.roomIdTextField tag:3001];
}

- (void)closeRoomAction:(BOOL)isEnableAudio isEnableVideo:(BOOL)isEnableVideo {

    [self.videoView setHidden:!isEnableVideo];
    self.emptImageView.hidden = isEnableVideo;
    self.buttonsView.enableVideo = isEnableVideo;
    [FeedShareMediaModel shared].enableVideo = isEnableVideo;
    self.buttonsView.enableAudio = isEnableAudio;
    [FeedShareMediaModel shared].enableAudio = isEnableAudio;
}

- (void)closeButtonClick {
    [self.navigationController popViewControllerAnimated:YES];
    [[FeedShareRTCManager shareRtc] disconnect];
}

- (void)loadDataWithClearUser {
    [FeedShareRTSManager clearUser:^(RTSACKModel * _Nonnull model) {
        
    }];
}

#pragma mark - getter

- (UIView *)roomTextView {
    if (!_roomTextView) {
        _roomTextView = [[UIView alloc] init];
        
        UIView *lineView = [[UIView alloc] init];
        lineView.backgroundColor = [UIColor.whiteColor colorWithAlphaComponent:0.76];
        [_roomTextView addSubview:lineView];
        [lineView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.right.bottom.equalTo(_roomTextView);
            make.height.mas_equalTo(1);
        }];
        
        [_roomTextView addSubview:self.roomIdTextField];
        [self.roomIdTextField mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.bottom.equalTo(_roomTextView);
            make.left.equalTo(_roomTextView).offset(12.5);
            make.right.equalTo(_roomTextView).offset(-12.5);
        }];
    }
    return _roomTextView;
}

- (UITextField *)roomIdTextField {
    if (!_roomIdTextField) {
        _roomIdTextField = [[UITextField alloc] init];
        _roomIdTextField.delegate = self;
        [_roomIdTextField setBackgroundColor:[UIColor clearColor]];
        [_roomIdTextField setTextColor:[UIColor whiteColor]];
        _roomIdTextField.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        [_roomIdTextField addTarget:self action:@selector(roomNumTextFieldChange:) forControlEvents:UIControlEventEditingChanged];
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"请输入房间ID" attributes:@{NSForegroundColorAttributeName : [[UIColor whiteColor] colorWithAlphaComponent:0.8]}];
        _roomIdTextField.attributedPlaceholder = attrString;
    }
    return _roomIdTextField;
}

- (UIButton *)enterRoomBtn {
    if (!_enterRoomBtn) {
        _enterRoomBtn = [[UIButton alloc] init];
        _enterRoomBtn.backgroundColor = [UIColor colorFromHexString:@"#1664FF"];
        _enterRoomBtn.layer.masksToBounds = YES;
        _enterRoomBtn.layer.cornerRadius = 50/2;
        _enterRoomBtn.titleLabel.font = [UIFont systemFontOfSize:16];
        [_enterRoomBtn setTitle:@"进入房间" forState:UIControlStateNormal];
        [_enterRoomBtn setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
        _enterRoomBtn.titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
        [_enterRoomBtn addTarget:self action:@selector(onClickEnterRoom:) forControlEvents:UIControlEventTouchUpInside];
        [self updateEnterRoomButtonColor:NO];
    }
    return _enterRoomBtn;
}

- (UIView *)videoView {
    if (!_videoView) {
        _videoView = [[UIView alloc] init];
    }
    return _videoView;
}

- (UITapGestureRecognizer *)tap {
    if (!_tap) {
        _tap = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                       action:@selector(tapGestureAction:)];
    }
    return _tap;
}

- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
    }
    return _contentView;
}

- (UIImageView *)emptImageView {
    if (!_emptImageView) {
        _emptImageView = [[UIImageView alloc] init];
        _emptImageView.image = [UIImage imageNamed:@"login_empt" bundleName:HomeBundleName];
        _emptImageView.hidden = YES;
    }
    return _emptImageView;
}

- (void)dealloc {
    [[FeedShareRTCManager shareRtc] disconnect];
    [PublicParameterComponent clear];
}

- (FeedShareCreateRoomButtonsView *)buttonsView {
    if (!_buttonsView) {
        _buttonsView = [[FeedShareCreateRoomButtonsView alloc] init];
        _buttonsView.delegate = self;
    }
    return _buttonsView;
}

- (BytedEffectProtocol *)beautyComponent {
    if (!_beautyComponent) {
        _beautyComponent = [[BytedEffectProtocol alloc] initWithRTCEngineKit:[FeedShareRTCManager shareRtc].rtcEngineKit];
    }
    return _beautyComponent;
}

- (UIButton *)closeButton {
    if (!_closeButton) {
        _closeButton = [[UIButton alloc] init];
        [_closeButton setImage:[UIImage imageNamed:@"feedshare_close_room_icon" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(closeButtonClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeButton;
}

- (UIView *)buttonBackView {
    if (!_buttonBackView) {
        _buttonBackView = [[UIView alloc] init];
        _buttonBackView.layer.cornerRadius = 15;
        _buttonBackView.backgroundColor = [UIColor.blackColor colorWithAlphaComponent:0.2];
    }
    return _buttonBackView;
}

- (FeedShareCreateRoomTipView *)tipView {
    if (!_tipView) {
        _tipView = [[FeedShareCreateRoomTipView alloc] init];
        _tipView.message = @"本产品仅用于功能体验，单次直播时长不超20分钟";
    }
    return _tipView;
}

@end
