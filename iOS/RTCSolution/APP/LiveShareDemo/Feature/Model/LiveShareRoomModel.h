// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveShareRoomStatus) {
  LiveShareRoomStatusChat = 1,
  LiveShareRoomStatusShare = 2,
};
typedef NS_ENUM(NSInteger, LiveShareVideoDirection) {
  LiveShareVideoDirectionHorizontal = 1,
  LiveShareVideoDirectionVertical = 2,
};

@interface LiveShareRoomModel : NSObject

@property(nonatomic, copy) NSString *appID;
@property(nonatomic, copy) NSString *roomID;
@property(nonatomic, copy) NSString *hostUid;
@property(nonatomic, copy) NSString *hostName;
@property(nonatomic, assign) LiveShareRoomStatus roomStatus;
@property(nonatomic, copy) NSString *rtcToken;
@property(nonatomic, copy) NSString *videoURL;
@property(nonatomic, assign) LiveShareVideoDirection videoDirection;

@end

NS_ASSUME_NONNULL_END
