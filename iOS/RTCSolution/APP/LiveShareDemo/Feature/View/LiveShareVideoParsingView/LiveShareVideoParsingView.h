// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@interface LiveShareVideoParsingView : UIView
@property(nonatomic, copy) void (^onCancelParsingBlock)(void);
- (void)showInview:(UIView *)view;

@end

NS_ASSUME_NONNULL_END
