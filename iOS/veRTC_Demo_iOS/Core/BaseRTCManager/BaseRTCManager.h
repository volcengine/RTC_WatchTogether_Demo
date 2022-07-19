//
//  BaseRTCManager.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/12/16.
//  Copyright © 2021 bytedance. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <VolcEngineRTC/objc/rtc/ByteRTCEngineKit.h>
#import <VolcEngineRTC/objc/rtc/ByteRTCRoom.h>
#import <YYModel/YYModel.h>
#import "RTMRequestModel.h"
#import "RTMACKModel.h"
#import "RTMNoticeModel.h"
#import "LocalUserComponents.h"
#import "PublicParameterCompoments.h"

NS_ASSUME_NONNULL_BEGIN

@protocol RTCNetworkProtocol <NSObject>

@optional

- (void)networkTypeChangedToType:(ByteRTCNetworkType)type;

- (void)didStartNetworkMonitoring;

- (void)didStopNetworkMonitoring;

@end

typedef void (^RTCRoomMessageBlock)(RTMNoticeModel *noticeModel);

@interface BaseRTCManager : NSObject <ByteRTCEngineDelegate, ByteRTCRoomDelegate>

@property (nonatomic, strong, nullable) ByteRTCEngineKit *rtcEngineKit;

@property (nonatomic, copy, nullable) void (^rtcJoinRoomBlock)(NSString *roomId, NSInteger errorCode, NSInteger joinType);

@property (nonatomic, weak, nullable) id<RTCNetworkProtocol> networkDelegate;

/// 业务标识参数
@property (nonatomic, copy, readonly) NSString *businessId;


/// 开启连接
/// @param scenes 场景标记
/// @param loginToken 用户登录token
/// @param block Callback
- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
          block:(void (^)(BOOL result))block;


/// 开启连接
/// @param scenes 场景标记
/// @param loginToken 用户登录token
/// @param contentPartner 合作方标识，对应控制台创建应用后生成的 “渠道号“，一起看场景使用
/// @param contentCategory 频道category，一起看场景使用
/// @param block Callback
- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
 contentPartner:(NSString *)contentPartner
contentCategory:(NSString *)contentCategory
          block:(void (^)(BOOL result))block;


/// 开启连接
/// @param scenes 场景标记
/// @param loginToken 用户登录token
/// @param volcAccountID 火山控制台账户id，录制功能使用
/// @param vodSpace 点播空间名，录制功能使用
/// @param block Callback
- (void)connect:(NSString *)scenes
     loginToken:(NSString *)loginToken
  volcAccountID:(NSString *)volcAccountID
       vodSpace:(NSString *)vodSpace
          block:(void (^)(BOOL result))block;

/// 关闭连接
- (void)disconnect;

/// 接口请求
- (void)emitWithAck:(NSString *)event
               with:(NSDictionary *)item
              block:(__nullable RTCSendServerMessageBlock)block;
           
/// 注册广播监听
- (void)onSceneListener:(NSString *)key
                  block:(RTCRoomMessageBlock)block;

/// 移除广播监听
- (void)offSceneListener;

/// 多房间,用于需要额外房间需求时使用
- (void)joinMultiRoomByToken:(NSString *)token
                    roomID:(NSString *)roomID
                    userID:(NSString *)userID;

/// 多房间,离开房间
- (void)leaveMultiRoom;

/// Get Sdk Version
+ (NSString *_Nullable)getSdkVersion;

#pragma mark - config

/// 父类每次初始化rtcEngineKit时会调用，子类直接覆写实现。
- (void)configeRTCEngine;

@end

NS_ASSUME_NONNULL_END
