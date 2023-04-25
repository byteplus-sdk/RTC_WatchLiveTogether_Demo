// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveSharePlayViewController.h"
#import "LiveShareRTSManager.h"
#import "LiveShareRoomModel.h"
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveShareRoomViewController : UIViewController

@property(nonatomic, weak) LiveSharePlayViewController *playController;

/**
 * @brief When a new user enters the room, execute this method after receiving the RTS message.
 * @param uid User model
 */
- (void)onUserJoined:(LiveShareUserModel *)userModel;


/**
 * @brief User checks out, execute this method after receiving RTS message.
 * @param userModel User model
 */
- (void)onUserLeaved:(LiveShareUserModel *)userModel;

/**
 * @brief The room state changes, and this method is executed after receiving an RTS message.
 * @param roomID RoomID
 * @param scene Room scene: chatting scene, watching live broadcast scene together
 * @param videoURL Live URL
 * @param videoDirection Video direction
 */
- (void)onUpdateRoomScene:(NSString *)roomID
                    scene:(LiveShareRoomStatus)scene
                 videoURL:(NSString *)videoURL
           videoDirection:(LiveShareVideoDirection)videoDirection;


/**
 * @brief This method is executed after receiving the RTS message when the live source of the room is changed.
 * @param roomID RoomID
 * @param userID UserID
 * @param videoURL Video URL
 * @param videoDirection Video direction
 */
- (void)onRoomVideoURLUpdated:(NSString *)roomID
                       userID:(NSString *)userID
                     videoURL:(NSString *)videoURL
               videoDorection:(LiveShareVideoDirection)videoDirection;


/**
 * @brief The user's media status is updated, and this method is executed after receiving an RTS message.
 * @param roomID RoomID
 * @param userModel User model
 */
- (void)onUserMediaUpdated:(NSString *)roomID
                 userModel:(LiveShareUserModel *)userModel;


/**
 * @brief This method is executed after receiving a user message and receiving an RTS message.
 * @param userModel User model
 * @param message Message content
 */
- (void)onReceivedUserMessage:(LiveShareUserModel *)userModel
                      message:(NSString *)message;


/**
 * @brief The room is closed, and the method is executed after receiving an RTS message.
 * @param roomID RoomID
 * @param type Close type
 */
- (void)onRoomClosed:(NSString *)roomID type:(LiveShareRoomCloseType)type;

@end

NS_ASSUME_NONNULL_END
