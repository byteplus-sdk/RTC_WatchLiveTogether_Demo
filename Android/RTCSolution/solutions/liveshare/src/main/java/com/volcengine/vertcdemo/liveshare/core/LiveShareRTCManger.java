// Copyright (c) 2023 BytePlus Pte. Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.liveshare.core;

import static com.ss.bytertc.engine.VideoCanvas.RENDER_MODE_HIDDEN;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.data.AudioPropertiesConfig;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.LocalAudioPropertiesInfo;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.RemoteAudioPropertiesInfo;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.ss.bytertc.engine.type.AudioProfileType;
import com.ss.bytertc.engine.type.AudioScenarioType;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.ErrorCode;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.volcengine.vertcdemo.core.eventbus.SDKReconnectToRoomEvent;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.common.MLog;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rts.RTCRoomEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTCVideoEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.liveshare.bean.AudioProperty;
import com.volcengine.vertcdemo.liveshare.bean.RTCErrorEvent;
import com.volcengine.vertcdemo.liveshare.bean.event.KickOutEvent;
import com.volcengine.vertcdemo.liveshare.bean.event.RTCLocalUserSpeakStatusEvent;
import com.volcengine.vertcdemo.liveshare.bean.event.RTCRemoteUserSpeakStatusEvent;
import com.volcengine.vertcdemo.liveshare.bean.event.RTCUserJoinEvent;
import com.volcengine.vertcdemo.liveshare.bean.event.RTCUserLeaveEvent;
import com.volcengine.vertcdemo.protocol.IEffect;
import com.volcengine.vertcdemo.protocol.ProtocolUtil;

import java.util.Observer;

/**
 * RTC object management class
 *
 * Use the singleton form, call the RTC interface, and update the LiveShareDataManager data in the call
 * Internal record switch status
 *
 * Function:
 * 1. Switch and media status
 * 2. Get the current media status
 * 3. Receive various RTC callbacks, such as: user check-in and check-out, media status change, media status data callback, network status callback, volume callback
 * 4. Manage user video rendering view
 * 5. Join and leave the room
 * 6. Create and destroy engine
 */
public class LiveShareRTCManger {
    private static final String TAG = "LiveShareRTCManger";

    private static LiveShareRTCManger sInstance = null;

    private RTCVideo mRTCVideo;
    private RTCRoom mRTCRoom;
    private LiveShareRTSClient mRTSClient;
    private String mRoomId;

    /**
     * Camera, microphone, camera direction data change listener.
     */
    private final Observer mMediaStatusObserver = (o, arg) -> {
        if (mRTCVideo == null) {
            return;
        }
        // Turn camera acquisition on and off.
        if (LiveShareDataManager.getInstance().getCameraMicManager().isCameraOn()) {
            mRTCVideo.startVideoCapture();
        } else {
            mRTCVideo.stopVideoCapture();
        }

        boolean isMicOn = LiveShareDataManager.getInstance().getCameraMicManager().isMicOn();
        if (isMicOn) {
            // Enable audio capture.
            mRTCVideo.startAudioCapture();
        }
        muteLocalAudioStream(!isMicOn);

        mRTCVideo.switchCamera(
                LiveShareDataManager.getInstance().getCameraMicManager().isFrontCamera()
                        ? CameraId.CAMERA_ID_FRONT
                        : CameraId.CAMERA_ID_BACK);

        setLocalVideoMirror();
    };

    public static LiveShareRTCManger ins() {
        if (sInstance == null) {
            sInstance = new LiveShareRTCManger();
        }
        return sInstance;
    }

    private final RTCVideoEventHandlerWithRTS mRTCVideoEventHandler = new RTCVideoEventHandlerWithRTS() {

        /**
         * Receive this callback after the first frame of remote video stream is received and decoded by SDK.
         * @param remoteStreamKey Remote Stream Information, see RemoteStreamKey for details.
         * @param frameInfo Video Frame Information, see VideoFrameInfo for details.
         */
        @Override
        public void onFirstRemoteVideoFrameDecoded(RemoteStreamKey remoteStreamKey, VideoFrameInfo frameInfo) {
            super.onFirstRemoteVideoFrameDecoded(remoteStreamKey, frameInfo);
            Log.d(TAG, "onFirstRemoteVideoFrameDecoded: " + remoteStreamKey.toString());

            String uid = remoteStreamKey.getUserId();
            if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(mRoomId)) {
                TextureView renderView = LiveShareDataManager.getInstance().getUserRenderView(uid);
                setRemoteVideoView(uid, renderView);
            }
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            Log.d(TAG, "onWarning: " + warn);
        }

        /**
         * Warning callback occurred.
         * @param err Error code. See IRTCEngineEventHandler.ErrorCode for details.
         */
        @Override
        public void onError(int err) {
            super.onError(err);
            Log.d(TAG, "onError: " + err);
            SolutionDemoEventManager.post(new RTCErrorEvent(err));
        }

        /**
         * remote Users audio information callback.
         * @param audioProperties Remote audio information，include audio stream properties,room id,
         *                            user id, see RemoteAudioPropertiesInfo class for details.
         * @param totalRemoteVolume Total volume of all of the remote streams.
         */
        @Override
        public void onRemoteAudioPropertiesReport(RemoteAudioPropertiesInfo[] audioProperties,
                                                  int totalRemoteVolume) {
            RTCRemoteUserSpeakStatusEvent event = new RTCRemoteUserSpeakStatusEvent();
            for (RemoteAudioPropertiesInfo item : audioProperties) {
                String uid = item.streamKey == null ? null : item.streamKey.getUserId();
                boolean isScreenStream = item.streamKey != null && item.streamKey.getStreamIndex() == StreamIndex.STREAM_INDEX_SCREEN;
                boolean speaking = item.audioPropertiesInfo != null && item.audioPropertiesInfo.linearVolume > 60;
                AudioProperty audioProperty = new AudioProperty(uid, isScreenStream, speaking);
                event.addAudioProperty(audioProperty);
            }
            SolutionDemoEventManager.post(event);
        }

        /**
         * Local audio properties info callback.
         * @param audioProperties Local audio info, see LocalAudioPropertiesInfo class for details.
         */
        @Override
        public void onLocalAudioPropertiesReport(LocalAudioPropertiesInfo[] audioProperties) {
            RTCLocalUserSpeakStatusEvent event = new RTCLocalUserSpeakStatusEvent();
            for (LocalAudioPropertiesInfo item : audioProperties) {
                boolean speaking = item.audioPropertiesInfo != null && item.audioPropertiesInfo.linearVolume > 60;
                boolean isScreenStream = item.streamIndex == StreamIndex.STREAM_INDEX_SCREEN;
                AudioProperty audioProperty = new AudioProperty(isScreenStream, speaking);
                event.addAudioProperty(audioProperty);
            }
            SolutionDemoEventManager.post(event);
        }
    };

    @SuppressWarnings("WriteOnlyObject")
    private final RTCRoomEventHandlerWithRTS mRTCRoomEventHandler = new RTCRoomEventHandlerWithRTS() {

        /**
         * Room status change callback, this callback will be received when joining a room,
         * leaving a room, or when a room-related warning or error occurs.
         * @param roomId room id
         * @param uid user id
         * @param state room status code
         * @param extraInfo extra information
         */
        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d(TAG, "onRoomStateChanged uid:" + uid + ",state:" + state);
            if (state == ErrorCode.ERROR_CODE_DUPLICATE_LOGIN) {
                SolutionDemoEventManager.post(new KickOutEvent());
                return;
            }
            if (!isFirstJoinRoomSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new RTCErrorEvent(state));
            } else if (isFirstJoinRoomSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new RTCErrorEvent(0));
            }
            if (isReconnectSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new SDKReconnectToRoomEvent(roomId));
            }
        }

        /**
         * Callback when visible users join the room, or invisible users in the room switch to visible.
         * @param userInfo user information
         * @param elapsed The elapsed time from when the host role user calls joinRoom to join the room to when other users in the room receive the event, in ms.
         */
        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            Log.d(TAG, String.format("onUserJoined : %s %d", userInfo, elapsed));

            // SDK 用户真实进房
            SolutionDemoEventManager.post(new RTCUserJoinEvent(userInfo.getUid(), true));
        }

        /**
         * When the remote user leaves the room or becomes invisible, the local user will receive this event
         * @param uid Remote user ID to leave room, or switch to invisible.
         * @param reason The reason why the user left the room:
         * • 0: The remote user calls leaveRoom to actively leave the room.
         * • 1: The remote user is disconnected due to Token expiration or network reasons.
         * • 2: The remote user calls setUserVisibility to switch to the invisible state.
         * • 3: The server calls OpenAPI to kick the remote user out of the room.
         */
        @Override
        public void onUserLeave(String uid, int reason) {
            Log.d(TAG, "onUserLeave uid: " + uid + ",reason:" + reason);
            SolutionDemoEventManager.post(new RTCUserLeaveEvent(uid));
        }
    };

    private LiveShareRTCManger() {}

    /**
     * initialize RTC.
     */
    private void initEngine(String appId, String bid) {
        Log.d(TAG, String.format("initEngine: appId: %s", appId));
        destroyRTCEngine();
        mRTCVideo = RTCVideo.createRTCVideo(AppUtil.getApplicationContext(), appId, mRTCVideoEventHandler, null, null);
        mRTCVideo.setBusinessId(bid);
        mRTCVideo.enableAudioPropertiesReport(new AudioPropertiesConfig(500, false, true));
        setLocalVideoMirror();

        observeMediaStatus();

        initVideoEffect();
    }

    public void rtcConnect(@NonNull RTSInfo info) {
        initEngine(info.appId, info.bid);
        mRTSClient = new LiveShareRTSClient(mRTCVideo, info);
        mRTCVideoEventHandler.setBaseClient(mRTSClient);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
    }

    public RTCVideo getRTCEngine() {
        return mRTCVideo;
    }

    public LiveShareRTSClient getRTSClient() {
        return mRTSClient;
    }

    /**
     * Destroy RTC engine.
     */
    public void destroyRTCEngine() {
        if (mRTCRoom != null) {
            mRTCRoom.destroy();
        }
        if (mRTCVideo != null) {
            RTCVideo.destroyRTCVideo();
            mRTCVideo = null;
        }
        stopObserveMediaStatus();
    }

    public void adjustUserVolume(int volume) {
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.setPlaybackVolume(volume);
    }

    /**
     * Turn volume ducking on/off.
     * @param enable ture: turn on volume ducking.
     *               false: turn off volume ducking.
     */
    public void enablePlaybackDucking(boolean enable) {
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.enablePlaybackDucking(enable);
    }

    public void setLocalVideoMirrorMode(MirrorType mode) {
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.setLocalVideoMirrorType(mode);
    }

    /**
     * Set remote video view.
     * @param userId User id.
     * @param textureView User render view.
     */
    public void setRemoteVideoView(String userId, TextureView textureView) {
        mRoomId = mRoomId == null ? "" : mRoomId;
        Log.d(TAG, String.format("setRemoteVideoView : %s %s", userId, mRoomId));
        if (mRTCVideo != null) {
            VideoCanvas canvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN);
            RemoteStreamKey remoteStreamKey = new RemoteStreamKey(mRoomId, userId, StreamIndex.STREAM_INDEX_MAIN);
            mRTCVideo.setRemoteVideoCanvas(remoteStreamKey, canvas);
        }
    }

    /**
     * Join the RTC room.
     * @param token  Dynamic key. It is used to authenticate users who enter the room.
     *               A Token is required to enter the room. During the test,
     *               you can use the console to generate a temporary Token.
     *               To officially go online, you need to use the key SDK to
     *               generate and issue a Token on your server.
     * @param roomId RTC room id.
     * @param userId User id.
     */
    public void joinRoom(String token, String roomId, String userId) {
        MLog.d("joinRoom", "token:" + token + " roomId:" + roomId + " userId:" + userId);
        leaveRoom();
        if (mRTCVideo == null) {
            return;
        }
        mRoomId = roomId;
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mRTCRoomEventHandler);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
        UserInfo userInfo = new UserInfo(userId, null);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, true);
        mRTCRoom.joinRoom(token, userInfo, roomConfig);

        mRTCVideo.setAudioScenario(AudioScenarioType.AUDIO_SCENARIO_COMMUNICATION);
        mRTCVideo.setAudioProfile(AudioProfileType.AUDIO_PROFILE_STANDARD);

        muteLocalAudioStream(!LiveShareDataManager.getInstance().getCameraMicManager().isMicOn());
        turnOnCamera(LiveShareDataManager.getInstance().getCameraMicManager().isCameraOn());
    }

    /**
     * Leave the room.
     */
    public void leaveRoom() {
        MLog.d("leaveRoom", "");
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
        mRTCRoom = null;
    }

    /**
     * Mute local audio stream.
     * @param mute true: Mute local audio stream.
     *             false: Cancel mute the local audio stream.
     */
    public void muteLocalAudioStream(boolean mute) {
        MLog.d("muteLocalAudioStream", "");
        if (mRTCRoom == null) {
            return;
        }
        if (mute) {
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        } else {
            mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        }
    }

    /**
     * Register camera, microphone, camera direction data change monitoring.
     */
    public void observeMediaStatus() {
        LiveShareDataManager.getInstance().getCameraMicManager().addObserver(mMediaStatusObserver);
    }

    /**
     * Cancel the camera, microphone, and camera direction data change monitoring.
     */
    public void stopObserveMediaStatus() {
        LiveShareDataManager.getInstance().getCameraMicManager().deleteObserver(mMediaStatusObserver);
    }

    /**
     * Switch the camera on or off.
     * @param isCameraOn Whether to open the camera.
     */
    private void turnOnCamera(boolean isCameraOn) {
        if (mRTCVideo != null) {
            if (isCameraOn) {
                mRTCVideo.startVideoCapture();
            } else {
                mRTCVideo.stopVideoCapture();
            }
        }
    }

    /**
     * Set up video mirroring, mirroring is enabled for the front camera,
     * and mirroring is not enabled for the rear camera.
     */
    private void setLocalVideoMirror() {
        boolean isFrontCamera = LiveShareDataManager.getInstance().getCameraMicManager().isFrontCamera();
        LiveShareRTCManger.ins().setLocalVideoMirrorMode(isFrontCamera
                ? MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER
                : MirrorType.MIRROR_TYPE_NONE);
    }

    /**
     * Initialize video effect.
     */
    private void initVideoEffect() {
        IEffect effect = ProtocolUtil.getIEffect();
        if (effect != null) {
            effect.initWithRTCVideo(mRTCVideo);
        }
    }

    /**
     * Open effect dialog.
     * @param context Context object.
     */
    public void openEffectDialog(Context context) {
        IEffect effect = ProtocolUtil.getIEffect();
        if (effect != null) {
            effect.showEffectDialog(context, null);
        }
    }
}
