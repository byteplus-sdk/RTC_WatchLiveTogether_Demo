// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareRoomViewController+Listener.h"
#import "LiveShareRTSManager.h"

@implementation LiveShareRoomViewController (Listener)

- (void)addListener {
    __weak typeof(self) weakSelf = self;
    // Add user entry notification
    [LiveShareRTSManager onUserJoinedBlock:^(LiveShareUserModel * _Nonnull userModel) {
        [weakSelf onUserJoined:userModel];
    }];
    // Add user leave notification
    [LiveShareRTSManager onUserLeavedBlock:^(LiveShareUserModel * _Nonnull userModel) {
        [weakSelf onUserLeaved:userModel];
    }];
    // Added room status change notification
    [LiveShareRTSManager onUpdateRoomSceneWithBlock:^(NSString * _Nonnull roomID, LiveShareRoomStatus roomStatus, NSString * _Nonnull userID, NSString * _Nonnull videoURL, LiveShareVideoDirection videoDirection) {
        [weakSelf onUpdateRoomScene:roomID scene:roomStatus videoURL:videoURL videoDirection:videoDirection];
    }];
    // Add homeowner update URL notification
    [LiveShareRTSManager onRoomVideoURLUpdatedBlock:^(NSString * _Nonnull roomID, NSString * _Nonnull videoURL, NSString * _Nonnull userID, LiveShareVideoDirection videoDirection) {
        [weakSelf onRoomVideoURLUpdated:roomID userID:userID videoURL:videoURL videoDorection:videoDirection];
    }];
    // Add user media state change notification
    [LiveShareRTSManager onUserMediaUpdatedBlock:^(NSString * _Nonnull roomID, LiveShareUserModel * _Nonnull userModel) {
        [weakSelf onUserMediaUpdated:roomID userModel:userModel];
    }];
    // Add user to send message notification
    [LiveShareRTSManager onReceivedUserMessageBlock:^(LiveShareUserModel * _Nonnull userModel, NSString * _Nonnull message) {
        [weakSelf onReceivedUserMessage:userModel message:message];
    }];
    // Add homeowner close room notification
    [LiveShareRTSManager onRoomClosedBlock:^(NSString * _Nonnull roomID, LiveShareRoomCloseType type) {
        [weakSelf onRoomClosed:roomID type:type];
    }];
}

@end
