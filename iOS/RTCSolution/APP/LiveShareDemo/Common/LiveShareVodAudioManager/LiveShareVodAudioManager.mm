// 
// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT
// 

#import "LiveShareVodAudioManager.h"
#import "ToolKit.h"

int VodAudioProcessorAudioMixingID = 0;

@interface LiveShareVodAudioManager ()

@property (nonatomic, weak) ByteRTCVideo *rtcKit;
@property (nonatomic, assign) int length;

@end

@implementation LiveShareVodAudioManager {
    int16_t* buffer;
    int _samplerate;
    int _channels;
}

- (instancetype)initWithRTCKit:(ByteRTCVideo *)rtcKit {
    if (self = [super init]) {
        self.rtcKit = rtcKit;
    }
    return self;
}

- (void)openAudio:(int)samplerate channels:(int)channels {
    _samplerate = samplerate;
    _channels = channels;
}

- (void)processAudio:(float **)inouts samples:(int)samples {
    
    int channelsCount = MIN(2, _channels);
    float gain = 1.0;
    int samplerate = _samplerate;
    
    int length = samples * channelsCount;
    if (self.length < length) {
        if (self.length > 0) {
            self.length = 0;
            delete [] (buffer);
        }
        buffer = new int16_t[length*2];
        self.length = length*2;
    }
    
    for (int i = 0; i < channelsCount; i++) {
        int offset = i;
        float *dataList = inouts[i];
        for (int j = 0; j < samples; j++) {
            float data = dataList[j];
            int value = gain * data * INT16_MAX;
            if(value > INT16_MAX){
                value = INT16_MAX;
            } else if(value < INT16_MIN){
                value = INT16_MIN;
            }
            buffer[offset] = value;
            offset += channelsCount;
        }
    }
    
    ByteRTCAudioFrame *frame = [[ByteRTCAudioFrame alloc] init];
    frame.buffer = [NSData dataWithBytes:buffer length:length*2];
    
    frame.samples = samples;
    frame.channel = (ByteRTCAudioChannel)channelsCount;
    frame.sampleRate = (ByteRTCAudioSampleRate)samplerate;
    
    ByteRTCMediaPlayer *manager = [self.rtcKit getMediaPlayer:VodAudioProcessorAudioMixingID];
    [manager pushExternalAudioFrame:frame];
}

- (void)dealloc {
    if (self.length > 0) {
        delete [] buffer;
        self.length = 0;
    }
}

@end
