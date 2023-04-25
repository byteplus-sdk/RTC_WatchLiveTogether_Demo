// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * Media state synchronization Model
 */
@interface LiveShareMediaModel : NSObject

@property(nonatomic, assign) BOOL enableAudio;

@property(nonatomic, assign) BOOL enableVideo;

+ (instancetype)shared;

- (void)resetMediaStatus;

@end

NS_ASSUME_NONNULL_END
