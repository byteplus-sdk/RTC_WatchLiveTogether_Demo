// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareRoomModel.h"
#import "LiveShareUserModel.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveShareDataManager : NSObject

@property(nonatomic, strong) LiveShareRoomModel *roomModel;

@property(nonatomic, assign, readonly) BOOL isHost;

+ (instancetype)shared;

+ (void)destroyDataManager;
- (void)addUserList:(NSArray<LiveShareUserModel *> *)userList;
- (void)addUser:(LiveShareUserModel *)userModel;
- (void)removeUser:(LiveShareUserModel *)userModel;
- (LiveShareUserModel *)getFullUserModel;

- (LiveShareUserModel *)getLocalUserModel;
- (NSArray<LiveShareUserModel *> *)getUserListWithoutFullUserList;
- (NSArray<LiveShareUserModel *> *)getAllUserList;
- (void)changeFullUserModelWithModel:(LiveShareUserModel *)model;
- (void)updateUserMedia:(LiveShareUserModel *)userModel;

@end

NS_ASSUME_NONNULL_END
