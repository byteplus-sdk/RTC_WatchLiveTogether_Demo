// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LiveShareVideoConfigModel : NSObject
+ (CGSize)defaultVideoSize;
+ (CGSize)watchingVideoSize;
+ (NSInteger)frameRate;
+ (NSInteger)maxKbps;

@end

NS_ASSUME_NONNULL_END
