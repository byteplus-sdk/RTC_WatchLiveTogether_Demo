// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class LiveShareBottomButtonsView;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, LiveShareButtonType) {
  LiveShareButtonTypeAudio = 1,
  LiveShareButtonTypeVideo = 2,
  LiveShareButtonTypeBeauty = 3,
  LiveShareButtonTypeWatch = 4,
  LiveShareButtonTypeSetting = 5,
};

typedef NS_ENUM(NSInteger, LiveShareButtonViewType) {
  LiveShareButtonViewTypePreView,
  LiveShareButtonViewTypeRoom,
  LiveShareButtonViewTypeWatch,
};

@protocol LiveShareBottomButtonsViewDelegate <NSObject>
- (void)liveShareBottomButtonsView:(LiveShareBottomButtonsView *)view
                didClickButtonType:(LiveShareButtonType)type;

@end
@interface LiveShareBottomButtonsView : UIView

@property(nonatomic, weak) id<LiveShareBottomButtonsViewDelegate> delegate;
@property(nonatomic, assign) BOOL enableAudio;
@property(nonatomic, assign) BOOL enableVideo;
- (instancetype)initWithType:(LiveShareButtonViewType)type;

@end

NS_ASSUME_NONNULL_END
