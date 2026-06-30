package com.mu.social.live

import android.content.Context
import io.agora.rtc2.*
import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgoraManager @Inject constructor() {
    private var rtcEngine: RtcEngine? = null
    
    private val _isJoined = MutableStateFlow(false)
    val isJoined = _isJoined.asStateFlow()

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid = _remoteUid.asStateFlow()

    private val engineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            _isJoined.value = true
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            _remoteUid.value = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            if (_remoteUid.value == uid) {
                _remoteUid.value = null
            }
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            _isJoined.value = false
            _remoteUid.value = null
        }
    }

    fun initEngine(context: Context, appId: String) {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = engineEventHandler
            rtcEngine = RtcEngine.create(config)
            
            rtcEngine?.enableVideo()
            rtcEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun joinChannel(token: String?, channelName: String, uid: Int, isBroadcaster: Boolean) {
        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        options.clientRoleType = if (isBroadcaster) {
            Constants.CLIENT_ROLE_BROADCASTER
        } else {
            Constants.CLIENT_ROLE_AUDIENCE
        }
        
        rtcEngine?.joinChannel(token, channelName, uid, options)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun toggleMicrophone(isMuted: Boolean) {
        rtcEngine?.muteLocalAudioStream(isMuted)
    }

    fun toggleBeautyFilter(enabled: Boolean) {
        val options = BeautyOptions()
        options.lighteningLevel = 0.7f
        options.smoothnessLevel = 0.5f
        options.rednessLevel = 0.1f
        rtcEngine?.setBeautyEffectOptions(enabled, options)
    }

    fun setupLocalVideo(container: android.view.ViewGroup) {
        val surfaceView = RtcEngine.CreateRendererView(container.context)
        container.addView(surfaceView)
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun setupRemoteVideo(container: android.view.ViewGroup, uid: Int) {
        val surfaceView = RtcEngine.CreateRendererView(container.context)
        container.addView(surfaceView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
