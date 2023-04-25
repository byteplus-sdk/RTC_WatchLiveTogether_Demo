// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
@class LiveShareVideoComponent;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveShareVideoState) {
  LiveShareVideoStateSuccess,
  LiveShareVideoStateFailure,
  LiveShareVideoStateCompleted,
};

@protocol LiveShareVideoComponentDelegate <NSObject>
- (void)liveShareVideoComponent:(LiveShareVideoComponent *)videoComponent
            onVideoStateChanged:(LiveShareVideoState)state
                          error:(NSError *)error;

@end
@interface LiveShareVideoComponent : NSObject

@property(nonatomic, weak) id<LiveShareVideoComponentDelegate> delegate;

- (instancetype)initWithSuperview:(UIView *)superView;
- (void)playWihtURLString:(NSString *)urlString;
- (void)stop;
- (void)close;

@end

NS_ASSUME_NONNULL_END
