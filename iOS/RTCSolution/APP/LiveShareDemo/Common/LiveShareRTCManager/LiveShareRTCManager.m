// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareRTCManager.h"
#import "LiveShareVideoConfigModel.h"
#import "LiveShareVodAudioManager.h"

@interface LiveShareRTCManager ()<ByteRTCVideoDelegate>

@property (nonatomic, assign) int audioMixingID;
@property (nonatomic, assign) ByteRTCCameraID cameraID;
@property (nonatomic, strong) NSMutableDictionary<NSString *, UIView *> *streamViewDic;
@property (nonatomic, strong) ByteRTCVideoEncoderConfig *videoEncoderConfig;

// RTC / RTS room object
@property (nonatomic, strong, nullable) ByteRTCRoom *rtcRoom;

@end

@implementation LiveShareRTCManager

+ (LiveShareRTCManager *)shareRtc {
    static LiveShareRTCManager *rtcManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        rtcManager = [[LiveShareRTCManager alloc] init];
    });
    return rtcManager;
}

- (void)configeRTCEngine {
    // Set the RTC encoding resolution, frame rate, and bit rate.
    self.videoEncoderConfig.width = [LiveShareVideoConfigModel defaultVideoSize].width;
    self.videoEncoderConfig.height = [LiveShareVideoConfigModel defaultVideoSize].height;
    self.videoEncoderConfig.frameRate = [LiveShareVideoConfigModel frameRate];
    self.videoEncoderConfig.maxBitrate = [LiveShareVideoConfigModel maxKbps];
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.videoEncoderConfig];
    
    // Set up video mirroring
    [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    
    _cameraID = ByteRTCCameraIDFront;
    _audioMixingID = 3001;
    VodAudioProcessorAudioMixingID = _audioMixingID;
}

- (void)joinRoomWithToken:(NSString *)token
                   roomID:(NSString *)roomID
                      uid:(NSString *)uid {
    // Join the room, start connecting the microphone, you need to apply for AppId and Token
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = uid;
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileCommunication;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    config.isAutoSubscribeVideo = YES;
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:roomID];
    self.rtcRoom.delegate = self;
    [self.rtcRoom joinRoom:token userInfo:userInfo roomConfig:config];
    // Set user visibility
    [self.rtcRoom setUserVisibility:YES];
    // Set the audio scene type
    [self.rtcEngineKit setAudioScenario:ByteRTCAudioScenarioCommunication];
    // Set initial call volume
    self.recordingVolume = 0.5;
    // Set initial mix volume
    self.audioMixingVolume = 0.1;
    // Set initial audio ducking state
    self.enableAudioDucking = NO;
    // Enable audio information prompts
    ByteRTCAudioPropertiesConfig *reportConfig = [[ByteRTCAudioPropertiesConfig alloc] init];
    reportConfig.interval = 500;
    [self.rtcEngineKit enableAudioPropertiesReport:reportConfig];
}

- (void)leaveRTCRoom {
    // Leave the RTC Room
    [self.rtcRoom leaveRoom];
    [self.streamViewDic removeAllObjects];
    [self switchCamera:ByteRTCCameraIDFront];
}

- (void)switchVideoCapture:(BOOL)isStart {
    // Start/Stop internal video capture.
    if (isStart) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeCamera
                                               block:^(BOOL isAuthorize) {
            if (isAuthorize) {
                [self.rtcEngineKit startVideoCapture];
            }
        }];
    } else {
        [self.rtcEngineKit stopVideoCapture];
    }
}

- (void)switchAudioCapture:(BOOL)isStart {
    // Start/Stop internal audio capture.
    if (isStart) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio
                                               block:^(BOOL isAuthorize) {
            if (isAuthorize) {
                [self.rtcEngineKit startAudioCapture];
            }
        }];
    } else {
        [self.rtcEngineKit stopAudioCapture];
    }
}

- (void)publishAudioStream:(BOOL)isPublish {
    // Publishes/Unpublish media streams captured by camera/microphone in the current room.
    if (isPublish) {
        [self.rtcRoom publishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcRoom unpublishStream:ByteRTCMediaStreamTypeAudio];
    }
}

- (void)switchCamera {
    if (self.cameraID == ByteRTCCameraIDFront) {
        self.cameraID = ByteRTCCameraIDBack;
    } else {
        self.cameraID = ByteRTCCameraIDFront;
    }
    // Switch to the front-facing/back-facing camera used in the internal video capture
    [self switchCamera:self.cameraID];
}

#pragma mark - Audio Mixing

- (void)startAudioMixing {
    // Set to see the resolution together
    self.videoEncoderConfig.width = [LiveShareVideoConfigModel watchingVideoSize].width;
    self.videoEncoderConfig.height = [LiveShareVideoConfigModel watchingVideoSize].height;
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.videoEncoderConfig];
    // Turn on the mix
    ByteRTCAudioMixingManager *manager = [self.rtcEngineKit getAudioMixingManager];
    [manager enableAudioMixingFrame:_audioMixingID type:ByteRTCAudioMixingTypePlayout];
}

- (void)stopAudioMixing {
    // Set call resolution
    self.videoEncoderConfig.width = [LiveShareVideoConfigModel defaultVideoSize].width;
    self.videoEncoderConfig.height = [LiveShareVideoConfigModel defaultVideoSize].height;
    [self.rtcEngineKit setMaxVideoEncoderConfig:self.videoEncoderConfig];
    // Turn off the mix
    ByteRTCAudioMixingManager *manager = [self.rtcEngineKit getAudioMixingManager];
    [manager disableAudioMixingFrame:_audioMixingID];
}

- (void)setRecordingVolume:(CGFloat)recordingVolume {
    // Adjust the mixed volume of all remote users playing locally [0, 1.0]
    _recordingVolume = recordingVolume;
    [self.rtcEngineKit setPlaybackVolume:(int)(recordingVolume*200)];
}

- (void)setAudioMixingVolume:(CGFloat)audioMixingVolume {
    // Adjust the volume of the mix [0, 1.0]
    _audioMixingVolume = audioMixingVolume;
    ByteRTCAudioMixingManager *audioMixingManager = [self.rtcEngineKit getAudioMixingManager];
    [audioMixingManager setAudioMixingVolume:_audioMixingID volume:(int)(_audioMixingVolume*100) type:ByteRTCAudioMixingTypePlayout];
}

- (void)setEnableAudioDucking:(BOOL)enableAudioDucking {
    _enableAudioDucking = enableAudioDucking;
    // Enable/disable audio ducking
    [self.rtcEngineKit enablePlaybackDucking:enableAudioDucking];
}

#pragma mark - Render

- (UIView *)getStreamViewWithUid:(NSString *)uid {
    if (IsEmptyStr(uid)) {
        return nil;
    }
    UIView *view = self.streamViewDic[uid];
    return view;
}

- (void)removeStreamViewWithUserID:(NSString *)userID {
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        [self.streamViewDic removeObjectForKey:userID];
    }));
}

- (void)bindCanvasViewWithUid:(NSString *)uid {
    dispatch_queue_async_safe(dispatch_get_main_queue(), (^{
        
        if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
            UIView *view = [self getStreamViewWithUid:uid];
            if (!view) {
                
                UIView *streamView = [[UIView alloc] init];
                streamView.backgroundColor = [UIColor clearColor];
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view = streamView;
                
                [self.rtcEngineKit setLocalVideoCanvas:ByteRTCStreamIndexMain
                                            withCanvas:canvas];
                [self.streamViewDic setValue:streamView forKey:uid];
            }
        } else {
            UIView *remoteRoomView = [self getStreamViewWithUid:uid];
            if (!remoteRoomView) {
                
                remoteRoomView = [[UIView alloc] init];
                remoteRoomView.backgroundColor = [UIColor clearColor];
                ByteRTCVideoCanvas *canvas = [[ByteRTCVideoCanvas alloc] init];
                canvas.renderMode = ByteRTCRenderModeHidden;
                canvas.view = remoteRoomView;
                
                ByteRTCRemoteStreamKey *streamKey = [[ByteRTCRemoteStreamKey alloc] init];
                streamKey.userId = uid;
                streamKey.roomId = self.rtcRoom.getRoomId;
                streamKey.streamIndex = ByteRTCStreamIndexMain;
                
                [self.rtcEngineKit setRemoteVideoCanvas:streamKey
                                             withCanvas:canvas];
                
                [self.streamViewDic setValue:remoteRoomView forKey:uid];
            }
        }
    }));
}

#pragma mark - ByteRTCRoomDelegate

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRoomStateChanged:(NSString *)roomId
        withUid:(NSString *)uid
          state:(NSInteger)state
      extraInfo:(NSString *)extraInfo {
    [super rtcRoom:rtcRoom onRoomStateChanged:roomId withUid:uid state:state extraInfo:extraInfo];
    
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        RTCJoinModel *joinModel = [RTCJoinModel modelArrayWithClass:extraInfo state:state roomId:roomId];
        if ([self.delegate respondsToSelector:@selector(liveShareRTCManager:onRoomStateChanged:)]) {
            [self.delegate liveShareRTCManager:self onRoomStateChanged:joinModel];
        }
    });
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onUserJoined:(ByteRTCUserInfo *)userInfo elapsed:(NSInteger)elapsed {
    // Remote visible user joins room, or room invisible user switches to visible
    [self bindCanvasViewWithUid:userInfo.userId];
}

#pragma mark - ByteRTCVideoDelegate

- (void)rtcEngine:(ByteRTCVideo *)engine onFirstRemoteVideoFrameDecoded:(ByteRTCRemoteStreamKey *)streamKey withFrameInfo:(ByteRTCVideoFrameInfo *)frameInfo {
    // Receive this callback after the first frame of remote video stream is received and decoded by SDK.
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(liveShareRTCManager:onFirstRemoteVideoFrameDecoded:)]) {
            [self.delegate liveShareRTCManager:self onFirstRemoteVideoFrameDecoded:streamKey.userId];
        }
    });
}

- (void)rtcEngine:(ByteRTCVideo *)engine onLocalAudioPropertiesReport:(NSArray<ByteRTCLocalAudioPropertiesInfo *> *)audioPropertiesInfos {
    // After calling enableAudioPropertiesReport, you will receive this callback periodically according to the interval value set
    
    NSInteger volume = 0;
    for (ByteRTCLocalAudioPropertiesInfo *info in audioPropertiesInfos) {
        if (info.streamIndex == ByteRTCStreamIndexMain) {
            volume = info.audioPropertiesInfo.linearVolume;
            break;
        }
    }
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        
        if ([self.delegate respondsToSelector:@selector(liveShareRTCManager:onLocalAudioPropertiesReport:)]) {
            [self.delegate liveShareRTCManager:self onLocalAudioPropertiesReport:volume];
        }
    });
    
}

- (void)rtcEngine:(ByteRTCVideo *)engine onRemoteAudioPropertiesReport:(NSArray<ByteRTCRemoteAudioPropertiesInfo *> *)audioPropertiesInfos totalRemoteVolume:(NSInteger)totalRemoteVolume {
    // After the remote user enters the room, after the local call enableAudioPropertiesReport, according to the set interval value, the local will periodically receive this callback
    
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    for (ByteRTCRemoteAudioPropertiesInfo *info in audioPropertiesInfos) {
        if (info.streamKey.streamIndex == ByteRTCStreamIndexMain) {
            [dict setValue:@(info.audioPropertiesInfo.linearVolume) forKey:info.streamKey.userId];
        }
    }
    
    dispatch_queue_async_safe(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(liveShareRTCManager:onReportRemoteUserAudioVolume:)]) {
            [self.delegate liveShareRTCManager:self onReportRemoteUserAudioVolume:dict];
        }
    });
}

#pragma mark - Private Action

- (void)switchCamera:(ByteRTCCameraID)cameraID {
    if (cameraID == ByteRTCCameraIDFront) {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeRenderAndEncoder];
    } else {
        [self.rtcEngineKit setLocalVideoMirrorType:ByteRTCMirrorTypeNone];
    }
    [self.rtcEngineKit switchCamera:cameraID];
}

#pragma mark - Getter

- (NSMutableDictionary<NSString *, UIView *> *)streamViewDic {
    if (!_streamViewDic) {
        _streamViewDic = [[NSMutableDictionary alloc] init];
    }
    return _streamViewDic;
}

- (ByteRTCVideoEncoderConfig *)videoEncoderConfig {
    if (!_videoEncoderConfig) {
        _videoEncoderConfig = [[ByteRTCVideoEncoderConfig alloc] init];
        _videoEncoderConfig.encoderPreference = ByteRTCVideoEncoderPreferenceDisabled;
    }
    return _videoEncoderConfig;
}

@end
