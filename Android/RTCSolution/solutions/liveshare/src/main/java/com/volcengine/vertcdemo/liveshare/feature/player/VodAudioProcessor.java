// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.feature.player;

import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.audio.IMediaPlayer;
import com.ss.bytertc.engine.data.AudioChannel;
import com.ss.bytertc.engine.data.AudioMixingType;
import com.ss.bytertc.engine.data.AudioSampleRate;
import com.ss.bytertc.engine.data.MediaPlayerConfig;
import com.ss.bytertc.engine.data.MediaPlayerCustomSource;
import com.ss.bytertc.engine.data.MediaPlayerCustomSourceMode;
import com.ss.bytertc.engine.data.MediaPlayerCustomSourceStreamType;
import com.ss.bytertc.engine.utils.AudioFrame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VodAudioProcessor {
    public static volatile int mixAudioGain = 100; // 0~100
    private static final int DEFAULT_MIX_ID = 0;
    private int mChannelCount;
    private int mSampleRate;
    private final RTCVideo mEngine;
    private final IMediaPlayer mixAudioManager;
    private ByteBuffer mRTCBuffer;

    private int mMixId = DEFAULT_MIX_ID;

    public void setMixId(int mixId) {
        this.mMixId = mixId;
    }

    public VodAudioProcessor(RTCVideo engine) {
        mEngine = engine;
        mixAudioManager = mEngine.getMediaPlayer(0);

        MediaPlayerCustomSource source = new MediaPlayerCustomSource();
        source.provider = null;
        source.mode = MediaPlayerCustomSourceMode.PUSH;
        source.type = MediaPlayerCustomSourceStreamType.RAW;

        MediaPlayerConfig config = new MediaPlayerConfig(AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT, 0);
        config.startPos = 0;
        config.syncProgressToRecordFrame = false;
        config.autoPlay = true;

        mixAudioManager.openWithCustomSource(source, config);
        mixAudioManager.setVolume(mixAudioGain, AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT);
    }

    public void audioOpen(int sampleRate, int channelCount) {
        this.mChannelCount = channelCount;
        this.mSampleRate = sampleRate;
    }

    public void audioProcess(ByteBuffer[] byteBuffers, int samples, long timestamp) {
        if (mEngine == null) {
            return;
        }
        int channelsCount = mChannelCount;
        // S16 格式一个声道中每个采样占两个字节
        int length = samples * channelsCount * 2;
        if (mRTCBuffer == null || mRTCBuffer.capacity() < length) {
            mRTCBuffer = ByteBuffer.allocate(length);
            mRTCBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        mRTCBuffer.clear();
        ByteBuffer srcBuffer0 = byteBuffers[0];
        srcBuffer0.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer srcBuffer1 = null;
        if (channelsCount > 1) {
            srcBuffer1 = byteBuffers[1];
            srcBuffer1.order(ByteOrder.LITTLE_ENDIAN);
        }
        // S16 双声道为 左右左右...
        for (int i = 0; i < samples; i++) {
            oneSampleFLTPtoS16(srcBuffer0, mRTCBuffer);
            if (srcBuffer1 != null) {
                oneSampleFLTPtoS16(srcBuffer1, mRTCBuffer);
            }
        }

        AudioChannel channel = getAudioChannel(mChannelCount);
        AudioSampleRate sampleRate = getAudioSampleRate(mSampleRate);
        AudioFrame frame = new AudioFrame(mRTCBuffer.array(), samples, sampleRate, channel);
        if (mixAudioManager != null) {
            mixAudioManager.setVolume(mixAudioGain, AudioMixingType.AUDIO_MIXING_TYPE_PLAYOUT);
            mixAudioManager.pushExternalAudioFrame(frame);
        }
    }

    /*
     * 将一个采样的数据格式从 FLTP 转换为 S16
     */
    private void oneSampleFLTPtoS16(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        float sample = srcBuffer.getFloat();
        int value = (int) (1.0f * sample * Short.MAX_VALUE);
        if (value > Short.MAX_VALUE) {
            value = Short.MAX_VALUE;
        } else if (value < Short.MIN_VALUE) {
            value = Short.MIN_VALUE;
        }
        dstBuffer.putShort((short) (value));
    }

    private AudioSampleRate getAudioSampleRate(int sampleRate) {
        if (sampleRate == 8000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_8000;
        } else if (sampleRate == 16000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_16000;
        } else if (sampleRate == 32000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_32000;
        } else if (sampleRate == 44100) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_44100;
        } else if (sampleRate == 48000) {
            return AudioSampleRate.AUDIO_SAMPLE_RATE_48000;
        }
        return AudioSampleRate.AUDIO_SAMPLE_RATE_AUTO;
    }

    private AudioChannel getAudioChannel(int channelCount) {
        if (channelCount == 1) {
            return AudioChannel.AUDIO_CHANNEL_MONO;
        } else if (channelCount == 2) {
            return AudioChannel.AUDIO_CHANNEL_STEREO;
        }
        return AudioChannel.AUDIO_CHANNEL_AUTO;
    }

}
