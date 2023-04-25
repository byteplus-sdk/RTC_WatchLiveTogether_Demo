// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class LiveShareRoomModel;

NS_ASSUME_NONNULL_BEGIN

@interface LiveSharePlayViewController : UIViewController
- (void)popToCreateRoomViewController;
- (void)popToRoomViewController;
- (void)updateVideoURL;
- (void)updateUserVideoRender;
- (void)addIMModel:(BaseIMModel *)imModel;
- (void)onUserMediaUpdated:(NSString *)roomID
                 userModel:(LiveShareUserModel *)userModel;

- (void)updateLocalUserVolume:(NSInteger)volume;

- (void)updateRemoteUserVolume:(NSDictionary *)volumeDict;
- (void)destroy;

@end

NS_ASSUME_NONNULL_END
