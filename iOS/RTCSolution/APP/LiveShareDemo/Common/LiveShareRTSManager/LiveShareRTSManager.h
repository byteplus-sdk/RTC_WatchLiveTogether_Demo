// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareRoomModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveShareRoomCloseType) {
    // Host closed
    LiveShareRoomCloseTypeByHost = 1,
    // Timeout dismissal
    LiveShareRoomCloseTypeTimeout,
    // Review and close room
    LiveShareRoomCloseTypeReview,
};

@interface LiveShareRTSManager : NSObject

/**
 * @brief Join the room
 * @param roomID RoomID
 * @param block Callback
 */
+ (void)
    requestJoinRoomWithRoomID:(NSString *)roomID
                        block:(void (^)(LiveShareRoomModel *roomModel,
                                        NSArray<LiveShareUserModel *> *userList,
                                        RTSACKModel *model))block;

/**
 * @brief Leave the room
 * @param roomID RoomID
 * @param block Callback
 */
+ (void)requestLeaveRoom:(NSString *)roomID
                   block:(void (^)(RTSACKModel *model))block;

/**
 * @brief The anchor opens to watch together
 * @param roomID RoomID
 * @param urlString URL string
 * @param videoDirection video direction
 * @param block Callback
 */
+ (void)requestJoinWatch:(NSString *)roomID
               urlString:(NSString *)urlString
          videoDirection:(LiveShareVideoDirection)videoDirection
                   block:(void (^)(LiveShareRoomModel *roomModel,
                                   RTSACKModel *model))block;


/**
 * @brief Anchor quit watching together
 * @param roomID RoomID
 * @param block Callback
 */
+ (void)requestLeaveWatch:(NSString *)roomID
                    block:(void (^)(LiveShareRoomModel *roomModel,
                                    RTSACKModel *model))block;

/**
 * @brief The anchor changes the playback link
 * @param roomID RoomID
 * @param urlString URL string
 * @param videoDirection Video direction
 * @param block Callback
 */
+ (void)requestChangeVideo:(NSString *)roomID
                 urlString:(NSString *)urlString
            videoDirection:(LiveShareVideoDirection)videoDirection
                     block:(void (^)(LiveShareRoomModel *roomModel,
                                     RTSACKModel *model))block;


/**
 * @brief message
 * @param roomID Room ID
 * @param message Message
 * @param block Callback
 */
+ (void)sendMessage:(NSString *)roomID
            message:(NSString *)message
              block:(void (^)(RTSACKModel *model))block;


/**
 * @brief change media state
 * @param roomID RoomID
 * @param enableMic microphone status
 * @param enableCamera camera status
 * @param block Callback
 */
+ (void)requestChangeMediaStatus:(NSString *)roomID
                             mic:(BOOL)enableMic
                          camera:(BOOL)enableCamera
                           block:(void (^)(LiveShareUserModel *userModel,
                                           RTSACKModel *model))block;


/**
 * @brief Clean up user legacy state
 * @param block Callback
 */
+ (void)clearUser:(void (^)(RTSACKModel *model))block;


/**
 * @brief Reconnection after network disconnection
 * @param block Callback
 */
+ (void)reconnectWithBlock:(void (^)(LiveShareRoomModel *roomModel,
                                     NSArray<LiveShareUserModel *> *userList,
                                     RTSACKModel *model))block;


/**
 * @brief Get the audience list in the room
 * @param block Callback
 */
+ (void)getUserListStatusWithBlock:(void (^)(NSArray<LiveShareUserModel *> *userList,
                                             RTSACKModel *model))block;

#pragma mark - Notification Message

+ (void)onUserJoinedBlock:(void (^)(LiveShareUserModel *userModel))block;

+ (void)onUserLeavedBlock:(void (^)(LiveShareUserModel *userModel))block;

+ (void)onUpdateRoomSceneWithBlock:
    (void (^)(NSString *roomID, LiveShareRoomStatus roomStatus,
              NSString *userID, NSString *videoURL,
              LiveShareVideoDirection videoDirection))block;

+ (void)onRoomVideoURLUpdatedBlock:
    (void (^)(NSString *roomID, NSString *videoURL, NSString *userID,
              LiveShareVideoDirection videoDirection))block;

+ (void)onUserMediaUpdatedBlock:(void (^)(NSString *roomID,
                                          LiveShareUserModel *userModel))block;

+ (void)onReceivedUserMessageBlock:(void (^)(LiveShareUserModel *userModel,
                                             NSString *message))block;

+ (void)onRoomClosedBlock:(void (^)(NSString *roomID,
                                    LiveShareRoomCloseType type))block;

@end

NS_ASSUME_NONNULL_END
