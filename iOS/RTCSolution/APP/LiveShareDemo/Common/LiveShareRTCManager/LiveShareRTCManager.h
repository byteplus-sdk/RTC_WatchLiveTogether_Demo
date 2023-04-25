// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "BaseRTCManager.h"
#import <Foundation/Foundation.h>
@class LiveShareRTCManager;

NS_ASSUME_NONNULL_BEGIN

@protocol LiveShareRTCManagerDelegate <NSObject>

/**
 * @brief Callback on room state changes. Via this callback you get notified of room relating warnings, errors and events. For example, the user joins the room, the user is removed from the room, and so on.
 * @param manager LiveShareRTCManager model
 * @param userID UserID
 */
- (void)liveShareRTCManager:(LiveShareRTCManager *)manager
         onRoomStateChanged:(RTCJoinModel *)joinModel;


/**
 * @brief User's first frame callback, refresh rendering UI
 * @param manager LiveShareRTCManager
 * @param userID UserID
 */
- (void)liveShareRTCManager:(LiveShareRTCManager *)manager
    onFirstRemoteVideoFrameDecoded:(NSString *)userID;

/**
 * @brief Local user volume change callback
 * @param manager RTC manager
 * @param volume user volume size
 */
- (void)liveShareRTCManager:(LiveShareRTCManager *)manager
    onLocalAudioPropertiesReport:(NSInteger)volume;

/**
 * @brief Remote user volume change callback
 * @param manager RTC manager
 * @param volumeInfo User volume information { UserID : volume size }
 */
- (void)liveShareRTCManager:(LiveShareRTCManager *)manager
    onReportRemoteUserAudioVolume:
        (NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo;

@end

@interface LiveShareRTCManager : BaseRTCManager

@property(nonatomic, weak) id<LiveShareRTCManagerDelegate> delegate;

+ (LiveShareRTCManager *)shareRtc;

/**
 * @brief Join the RTC room. When hosts or guests join a room, they need to join the live room first and then join the RTC room.
 * @param token RTC Token
 * @param roomID RTC room ID
 * @param uid RTC user ID
 */

/**
 * @brief 加入 RTC 房间。主播或嘉宾加入房间时，需要先加入直播房间再加入 RTC 房间。
 * @param token RTC Token
 * @param roomID RTC 房间 ID
 * @param uid RTC 用户 ID
 */
- (void)joinRoomWithToken:(NSString *)token
                   roomID:(NSString *)roomID
                      uid:(NSString *)uid;

/**
 * @brief Leave RTC room
 */
- (void)leaveRTCRoom;

/**
 * @brief Start/Stop local video capture
 * @param isStart ture: enable video capture false: disable video capture
 */
- (void)switchVideoCapture:(BOOL)isStart;


/**
 * @brief Start/Stop local audio capture
 * @param isStart ture: enable audio capture false: disable audio capture
 */
- (void)switchAudioCapture:(BOOL)isStart;

/**
 * @brief Front and rear camera switching
 */
- (void)switchCamera;

/**
 * @brief Control the sending status of the local audio stream: send/not send
 * @param mute ture:Not send, false：Send
 */
- (void)publishAudioStream:(BOOL)isPublish;

#pragma mark - Audio Mixing

// Adjust the volume of all remote user mixes played locally [0, 1.0]
@property(nonatomic, assign) CGFloat recordingVolume;

// Adjust the volume of the mix [0, 1.0]
@property(nonatomic, assign) CGFloat audioMixingVolume;

// Whether to enable audio ducking
@property(nonatomic, assign) BOOL enableAudioDucking;

/**
 * @brief Enable audio mixing
 */
- (void)startAudioMixing;

/**
 * @brief Turn off audio mixing
 */
- (void)stopAudioMixing;

#pragma mark - Render

/**
 * @brief Get RTC rendering UIView
 * @param uid User id
 */
- (UIView *)getStreamViewWithUid:(NSString *)uid;

/**
 * @brief remove render binding for local user
 * @param userID User id
 */
- (void)removeStreamViewWithUserID:(NSString *)userID;

/**
 * @brief user ID and RTC rendering View for binding
 * @param uid User id
 */
- (void)bindCanvasViewWithUid:(NSString *)uid;

@end

NS_ASSUME_NONNULL_END
