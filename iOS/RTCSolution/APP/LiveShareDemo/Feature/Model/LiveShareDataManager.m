// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareDataManager.h"

static LiveShareDataManager *manager = nil;
static dispatch_once_t onceToken;

@interface LiveShareDataManager ()

@property (nonatomic, strong) LiveShareUserModel *fullUserModel;

@property (nonatomic, copy) NSArray<LiveShareUserModel *> *userList;

@property (nonatomic, strong) LiveShareUserModel *localUserModel;

@end

@implementation LiveShareDataManager

+ (instancetype)shared {
    
    dispatch_once(&onceToken, ^{
        manager = [[LiveShareDataManager alloc] init];
        [manager initData];
    });
    return manager;
}

+ (void)destroyDataManager {
    manager = nil;
    onceToken = 0;
}
- (void)initData {
    self.userList = [NSMutableArray array];
}

- (void)setRoomModel:(LiveShareRoomModel *)roomModel {
    _roomModel = roomModel;
    
    _isHost = [roomModel.hostUid isEqualToString:[LocalUserComponent userModel].uid];
}
- (void)addUserList:(NSArray<LiveShareUserModel *> *)userList {
    
    NSMutableArray *array = userList.mutableCopy;
    
    for (int i = 0; i < array.count; i++) {
        LiveShareUserModel *model = array[i];
        if ([model.uid isEqualToString:[LocalUserComponent userModel].uid]) {
            self.localUserModel = model;
            [array removeObjectAtIndex:i];
            break;
        }
    }
    self.userList = array.copy;
}
- (void)addUser:(LiveShareUserModel *)userModel {
    NSMutableArray *array = self.userList.mutableCopy;
    NSUInteger index = [array indexOfObject:userModel];
    if (index != NSNotFound) {
        [array replaceObjectAtIndex:index withObject:userModel];
    }
    else {
        [array addObject:userModel];
    }
    self.userList = array.copy;
}
- (void)removeUser:(LiveShareUserModel *)userModel {
    NSMutableArray *array = self.userList.mutableCopy;
    [array removeObject:userModel];
    self.userList = array.copy;
}
- (LiveShareUserModel *)getFullUserModel {
    NSUInteger index = [self.userList indexOfObject:self.fullUserModel];
    if (index == NSNotFound) {
        self.fullUserModel = self.localUserModel;
    }
    else {
        self.fullUserModel = self.userList[index];
    }
    return self.fullUserModel;
}
- (NSArray<LiveShareUserModel *> *)getUserListWithoutFullUserList {
    NSMutableArray *array = self.userList.mutableCopy;
    [array removeObject:self.fullUserModel];
    return array.copy;
}
- (NSArray<LiveShareUserModel *> *)getAllUserList {
    return self.userList;
}
- (void)changeFullUserModelWithModel:(LiveShareUserModel *)model {
    self.fullUserModel = model;
}
- (void)updateUserMedia:(LiveShareUserModel *)userModel {
    
    if ([userModel.uid isEqualToString:[LocalUserComponent userModel].uid]) {
        self.localUserModel = userModel;
    }
    else {
        NSUInteger index = [self.userList indexOfObject:userModel];
        if (index == NSNotFound) {
            return;
        }
        NSMutableArray *array = self.userList.mutableCopy;
        [array replaceObjectAtIndex:index withObject:userModel];
        self.userList = array.copy;
    }
}

- (LiveShareUserModel *)getLocalUserModel {
    return self.localUserModel;
}

@end
