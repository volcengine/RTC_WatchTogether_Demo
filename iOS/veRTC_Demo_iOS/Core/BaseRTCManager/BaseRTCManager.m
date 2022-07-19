//
//  BaseRTCManager.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/12/16.
//  Copyright © 2021 bytedance. All rights reserved.
//

#import "BaseRTCManager.h"
#import "NetworkingManager.h"
#import "NetworkingTool.h"
#import "BuildConfig.h"
#import "Core.h"

typedef NSString* RTMMessageType;
static RTMMessageType const RTMMessageTypeResponse = @"return";
static RTMMessageType const RTMMessageTypeNotice = @"inform";

@interface BaseRTCManager ()

@property (nonatomic, copy) void (^rtcLoginBlock)(BOOL result);
@property (nonatomic, copy) void (^rtcSetParamsBlock)(BOOL result);
@property (nonatomic, strong) NSMutableDictionary *listenerDic;
@property (nonatomic, strong) NSMutableDictionary *senderDic;
@property (nonatomic, strong) ByteRTCRoom *multiRoom;

@end

@implementation BaseRTCManager

#pragma mark - Publish Action

- (instancetype)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
          block:(void (^)(BOOL result))block {
    [self connect:scenes
       loginToken:loginToken
    volcAccountID:@""
         vodSpace:@""
   contentPartner:@""
  contentCategory:@""
            block:block];
}

- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
 contentPartner:(NSString *)contentPartner
contentCategory:(NSString *)contentCategory
          block:(void (^)(BOOL result))block {
    [self connect:scenes
       loginToken:loginToken
    volcAccountID:@""
         vodSpace:@""
   contentPartner:contentPartner
  contentCategory:contentCategory
            block:block];
}

- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
  volcAccountID:(NSString *)volcAccountID
       vodSpace:(NSString *)vodSpace
          block:(void (^)(BOOL result))block {
    [self connect:scenes
       loginToken:loginToken
    volcAccountID:volcAccountID
         vodSpace:vodSpace
   contentPartner:@""
  contentCategory:@""
            block:block];
}

- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
  volcAccountID:(NSString *)volcAccountID
       vodSpace:(NSString *)vodSpace
 contentPartner:(NSString *)contentPartner
contentCategory:(NSString *)contentCategory
          block:(void (^)(BOOL result))block {
    NSString *errorMessage = @"";
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    if (NOEmptyStr(APPID)) {
        [dic setValue:APPID forKey:@"appId"];
    } else {
        errorMessage = @"APPID";
    }
    
    if (NOEmptyStr(APPKey)) {
        [dic setValue:APPKey forKey:@"appKey"];
    } else {
        errorMessage = @"APPKey";
    }
    
    if (NOEmptyStr(AccessKeyID)) {
        [dic setValue:AccessKeyID forKey:@"volcAk"];
    } else {
        errorMessage = @"AccessKeyID";
    }
    
    if (NOEmptyStr(SecretAccessKey)) {
        [dic setValue:SecretAccessKey forKey:@"volcSk"];
    } else {
        errorMessage = @"SecretAccessKey";
    }
    
    if (NOEmptyStr(scenes)) {
        [dic setValue:scenes forKey:@"scenesName"];
    } else {
        errorMessage = @"scenes";
    }
    
    if (NOEmptyStr(loginToken)) {
        [dic setValue:loginToken forKey:@"loginToken"];
    } else {
        errorMessage = @"loginToken";
    }
    if (NOEmptyStr(errorMessage)) {
        errorMessage = [NSString stringWithFormat:@"%@ 为空请查看配置", errorMessage];
        if (block) {
            block(NO);
        }
        return;
    }
    [PublicParameterCompoments share].appId = APPID;
    if (NOEmptyStr(volcAccountID)) {
        [dic setValue:volcAccountID forKey:@"volcAccountID"];
    }
    if (NOEmptyStr(vodSpace)) {
        [dic setValue:vodSpace forKey:@"vodSpace"];
    }
    if (NOEmptyStr(contentPartner)) {
        [dic setValue:contentPartner forKey:@"contentPartner"];
    }
    if (NOEmptyStr(contentCategory)) {
        [dic setValue:contentCategory forKey:@"contentCategory"];
    }
    __weak __typeof(self) wself = self;
    [NetworkingManager setAppInfoWithAppId:dic
                                    block:^(NetworkingResponse * _Nonnull response) {
        if (!response.result) {
            if (block) {
                block(NO);
            }
            return;
        }
        NSString *appID = response.response[@"app_id"];
        NSString *RTMToken = response.response[@"rtm_token"];
        NSString *serverUrl = response.response[@"server_url"];
        NSString *serverSig = response.response[@"server_signature"];
        NSString *bid = response.response[@"bid"];
        
        [wself rtcConnect:appID
                 RTMToken:RTMToken
                serverUrl:serverUrl
                serverSig:serverSig
                      bid:bid
                    block:block];
    }];
}

- (void)disconnect {
    [self.rtcEngineKit logout];
    [self.rtcEngineKit destroyEngine];
    self.rtcEngineKit = nil;
    self.rtcLoginBlock = nil;
    self.rtcSetParamsBlock = nil;
    self.rtcJoinRoomBlock = nil;
}


#pragma mark - SetAppInfo

- (void)emitWithAck:(NSString *)event
               with:(NSDictionary *)item
              block:(RTCSendServerMessageBlock)block {
    if (IsEmptyStr(event)) {
        [self throwErrorAck:RTMStatusCodeInvalidArgument
                    message:@"缺少EventName"
                      block:block];
        return;
    }
    NSString *appId = @"";
    NSString *roomId = @"";
    if ([item isKindOfClass:[NSDictionary class]]) {
        appId = item[@"app_id"];
        roomId = item[@"room_id"];
        if (IsEmptyStr(appId)) {
            [self throwErrorAck:RTMStatusCodeInvalidArgument
                        message:@"缺少AppID"
                          block:block];
            return;
        }
    }
    NSString *wisd = [NetworkingTool getWisd];
    
    RTMRequestModel *requestModel = [[RTMRequestModel alloc] init];
    requestModel.eventName = event;
    requestModel.app_id = appId;
    requestModel.roomID = roomId;
    requestModel.userID = [LocalUserComponents userModel].uid;
    requestModel.requestID = [NetworkingTool MD5ForLower16Bate:wisd];
    requestModel.content = [item yy_modelToJSONString];
    requestModel.deviceID = [NetworkingTool getDeviceId];
    requestModel.requestBlock = block;
    
    NSString *json = [requestModel yy_modelToJSONString];
    requestModel.msgid = (NSInteger)[self.rtcEngineKit sendServerMessage:json];
    
    NSString *key = requestModel.requestID;
    [self.senderDic setValue:requestModel forKey:key];
    [self addLog:@"发送业务服务器消息" message:json];
}
           
- (void)onSceneListener:(NSString *)key
                  block:(RTCRoomMessageBlock)block {
    if (IsEmptyStr(key)) {
        return;
    }
    [self.listenerDic setValue:block forKey:key];
}

- (void)offSceneListener {
    [self.listenerDic removeAllObjects];
}

- (void)joinMultiRoomByToken:(NSString *)token
                      roomID:(NSString *)roomID
                      userID:(NSString *)userID {
    if (self.multiRoom != nil) {
        [self leaveMultiRoom];
    }
    self.multiRoom = [self.rtcEngineKit createRtcRoom:roomID];
    [self.multiRoom setRtcRoomDelegate:self];
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = userID;

    ByteRTCMultiRoomConfig *config = [[ByteRTCMultiRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileLiveBroadcasting;
    config.isAutoSubscribeAudio = NO;
    config.isAutoSubscribeVideo = NO;
    [self.multiRoom joinRoomByToken:token userInfo:userInfo roomConfig:config];
}

- (void)leaveMultiRoom {
    [self.multiRoom leaveRoom];
    [self.multiRoom destroy];
    self.multiRoom = nil;
}

#pragma mark - config
- (void)configeRTCEngine {
    
}

#pragma mark - ByteRTCEngineDelegate

- (void)rtcEngine:(ByteRTCEngineKit *)engine onWarning:(ByteRTCWarningCode)Code {
    NSLog(@"[%@]-OnWarning %ld", [self class], (long)Code);
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine onError:(ByteRTCErrorCode)errorCode {
    NSLog(@"[%@]-OnError %ld", [self class], (long)errorCode);
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine connectionChangedToState:(ByteRTCConnectionState)state {
    NSLog(@"[%@]-ConnectionChangedToState %ld", [self class], (long)state);
}

- (void)rtcEngine:(ByteRTCEngineKit *)engine networkTypeChangedToType:(ByteRTCNetworkType)type {
    NSLog(@"[%@]-NetworkTypeChangedToType %ld", [self class], (long)type);
}

// 收到登录结果
- (void)rtcEngine:(ByteRTCEngineKit *)engine
    onLoginResult:(NSString *)uid
        errorCode:(ByteRTCLoginErrorCode)errorCode
          elapsed:(NSInteger)elapsed {
    if (self.rtcLoginBlock) {
        self.rtcLoginBlock((errorCode == ByteRTCLoginErrorCodeSuccess) ? YES : NO);
    }
    NSLog(@"[%@]-LoginResult code %ld", [self class], (long)errorCode);
}

- (void)rtcEngineOnLogout:(ByteRTCEngineKit * _Nonnull)engine {
    
}

// 收到业务服务器参数设置结果
- (void)rtcEngine:(ByteRTCEngineKit *)engine onServerParamsSetResult:(NSInteger)errorCode {
    if (self.rtcSetParamsBlock) {
        self.rtcSetParamsBlock((errorCode == RTMStatusCodeSuccess) ? YES : NO);
    }
    NSLog(@"[%@]-ServerParamsSetResult code %ld", [self class], (long)errorCode);
}

// 收到加入房间结果
- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    NSDictionary *dic = [self dictionaryWithJsonString:extraInfo];
    NSInteger errorCode = state;
    NSInteger joinType = -1;
    if ([dic isKindOfClass:[NSDictionary class]]) {
        NSString *joinTypeStr = [NSString stringWithFormat:@"%@", dic[@"join_type"]];
        joinType = joinTypeStr.integerValue;
    }
    if (self.rtcJoinRoomBlock) {
        void (^rtcJoinRoomBlock)(NSString *, NSInteger, NSInteger) = self.rtcJoinRoomBlock;
        dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
            rtcJoinRoomBlock(roomId, errorCode, joinType);
        });
    }
}

// 收到消息发送结果
- (void)rtcEngine:(ByteRTCEngineKit *)engine
    onServerMessageSendResult:(int64_t)msgid
            error:(ByteRTCUserMessageSendResult)error {
    if (error == ByteRTCUserMessageSendResultSuccess) {
        // 发送成功，等待业务回调信息
        return;
    }
    // 发送失败 msgid 开始
    NSString *key = @"";
    for (RTMRequestModel *model in self.senderDic.allValues) {
        if (model.msgid == msgid) {
            key = model.requestID;
            [self throwErrorAck:RTMStatusCodeSendMessageFaild
                        message:[NetworkingTool messageFromResponseCode:RTMStatusCodeSendMessageFaild]
                          block:model.requestBlock];
            NSLog(@"[%@]-收到消息发送结果 %@ msgid %lld request_id %@ ErrorCode %ld", [self class], model.eventName, msgid, key, (long)error);
            break;
        }
    }
    if (NOEmptyStr(key)) {
        [self.senderDic removeObjectForKey:key];
    }
}

// 收到业务服务器发送的房间内点对点文本消息内容
- (void)rtcEngine:(ByteRTCEngineKit *)engine onUserMessageReceived:(NSString *)uid
          message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"收到业务服务器、房间内、点对点消息内容" message:message];
}

// 收到业务服务器发送的房间外点对点文本消息内容
- (void)rtcEngine:(ByteRTCEngineKit *)engine onUserMessageReceivedOutsideRoom:(NSString *)uid
          message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"收到业务服务器、房间外、点对点消息" message:message];
}

// 收到业务服务器发送的房间内文本广播消息内容
- (void)rtcEngine:(ByteRTCEngineKit *)engine onRoomMessageReceived:(NSString *)uid
          message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"收到业务服务器、房间内、广播消息" message:message];
}

#pragma mark - ByteRTCRoomDelegate
- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomWarning:(ByteRTCWarningCode)warningCode {
    NSLog(@"[%@]-OnRoomWarning %ld", [self class], (long)warningCode);
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomError:(ByteRTCErrorCode)errorCode {
    NSLog(@"[%@]-OnRoomError %ld", [self class], (long)errorCode);
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomStateChanged:(NSString *)roomId withUid:(NSString *)uid state:(NSInteger)state extraInfo:(NSString *)extraInfo {
    NSDictionary *dic = [self dictionaryWithJsonString:extraInfo];
    NSInteger errorCode = state;
    NSInteger joinType = -1;
    if ([dic isKindOfClass:[NSDictionary class]]) {
        NSString *joinTypeStr = [NSString stringWithFormat:@"%@", dic[@"join_type"]];
        joinType = joinTypeStr.integerValue;
    }
    if (self.rtcJoinRoomBlock) {
        void (^rtcJoinRoomBlock)(NSString *, NSInteger, NSInteger) = self.rtcJoinRoomBlock;
        dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
            rtcJoinRoomBlock(roomId, errorCode, joinType);
        });
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomMessageReceived:(NSString *)uid message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"收到房间消息" message:message];
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserMessageReceived:(NSString *)uid message:(NSString *)message {
    [self dispatchMessageFrom:uid message:message];
    [self addLog:@"收到单点消息" message:message];
}

#pragma mark - Private Action

- (void)rtcConnect:(NSString *)appID
          RTMToken:(NSString *)RTMToken
         serverUrl:(NSString *)serverUrl
         serverSig:(NSString *)serverSig
               bid:(NSString *)bid
             block:(void (^)(BOOL result))block {
    NSString *uid = [LocalUserComponents userModel].uid;
    if (IsEmptyStr(uid)) {
        if (block) {
            block(NO);
        }
        return;
    }
    if (self.rtcEngineKit) {
        [self.rtcEngineKit destroyEngine];
        self.rtcEngineKit = nil;
    }
    self.rtcEngineKit = [[ByteRTCEngineKit alloc] initWithAppId:appID
                                                       delegate:self
                                                     parameters:@{}];
    _businessId = bid;
    [self.rtcEngineKit setBusinessId:bid];
    [self configeRTCEngine];
    [self.rtcEngineKit login:RTMToken uid:uid];
    __weak __typeof(self) wself = self;
    self.rtcLoginBlock = ^(BOOL result) {
        wself.rtcLoginBlock = nil;
        if (result) {
            [wself.rtcEngineKit setServerParams:serverSig url:serverUrl];
        } else {
            wself.rtcSetParamsBlock = nil;
            if (block) {
                dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                    block(result);
                });
            }
        }
    };
    self.rtcSetParamsBlock = ^(BOOL result) {
        wself.rtcSetParamsBlock = nil;
        if (block) {
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                block(result);
            });
        }
    };
}

- (void)dispatchMessageFrom:(NSString *)uid message:(NSString *)message {
    NSDictionary *dic = [NetworkingTool decodeJsonMessage:message];
    if (!dic || !dic.count) {
        return;
    }
    NSString *messageType = dic[@"message_type"];
    if ([messageType isEqualToString:RTMMessageTypeResponse]) {
        [self receivedResponseFrom:uid object:dic];
        return;
    }
    
    if ([messageType isEqualToString:RTMMessageTypeNotice]) {
        [self receivedNoticeFrom:uid object:dic];
        return;
    }
}

// 业务服务器收到客户端请求后返回的数据结果处理
- (void)receivedResponseFrom:(NSString *)uid object:(NSDictionary *)object {
    RTMACKModel *ackModel = [RTMACKModel modelWithMessageData:object];
    if (IsEmptyStr(ackModel.requestID)) {
        return;
    }
    NSString *key = ackModel.requestID;
    RTMRequestModel *model = self.senderDic[key];
    if (model && [model isKindOfClass:[RTMRequestModel class]]) {
        if (model.requestBlock) {
            dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
                model.requestBlock(ackModel);
            });
        }
    }
    [self.senderDic removeObjectForKey:key];
}

// 收到服务端通知处理
- (void)receivedNoticeFrom:(NSString *)uid object:(NSDictionary *)object {
    RTMNoticeModel *noticeModel = [RTMNoticeModel yy_modelWithJSON:object];
    if (IsEmptyStr(noticeModel.eventName)) {
        return;
    }
    RTCRoomMessageBlock block = self.listenerDic[noticeModel.eventName];
    if (block) {
        dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
            block(noticeModel);
        });
    }
}

- (void)throwErrorAck:(NSInteger)code message:(NSString *)message
                block:(__nullable RTCSendServerMessageBlock)block {
    if (!block) {
        return;
    }
    RTMACKModel *ackModel = [[RTMACKModel alloc] init];
    ackModel.code = code;
    ackModel.message = message;
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        block(ackModel);
    });
}

+ (NSString *_Nullable)getSdkVersion {
    return [ByteRTCEngineKit getSdkVersion];
}

#pragma mark - Getter

- (NSMutableDictionary *)listenerDic {
    if (!_listenerDic) {
        _listenerDic = [[NSMutableDictionary alloc] init];
    }
    return _listenerDic;
}

- (NSMutableDictionary *)senderDic {
    if (!_senderDic) {
        _senderDic = [[NSMutableDictionary alloc] init];
    }
    return _senderDic;
}

#pragma mark - Tool

- (void)addLog:(NSString *)key message:(NSString *)message {
    NSLog(@"[%@]-%@ %@", [self class], key, [NetworkingTool decodeJsonMessage:message]);
}

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
   if (jsonString == nil) {
       return nil;
   }

   NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
   NSError *err;
   NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                       options:NSJSONReadingMutableContainers
                                                         error:&err];
   if(err) {
       NSLog(@"json解析失败：%@",err);
       return nil;
   }
   return dic;
}

@end
