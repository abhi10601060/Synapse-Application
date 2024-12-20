package com.example.synapse.viemodel

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.ChatMessage
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.network.Resource
import com.example.synapse.repo.StreamRepo
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import livekit.org.webrtc.RendererCommon
import javax.inject.Inject

const val STREAM_STATUS_IDLE = "idle"
const val STREAM_STATUS_GOING_LIVE  = "starting"
const val STREAM_STATUS_LIVE = "live"
const val STREAM_STATUS_LIVE_ERROR = "live_error"
const val STREAM_STATUS_STOPPING = "stopping"
const val STREAM_STATUS_STOPPED = "stopped"
const val STREAM_STATUS_STOP_ERROR = "stop_error"

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val streamRepo: StreamRepo,
    private val gson : Gson,
    private val sharedprefUtil: SharedprefUtil
)  : ViewModel(){

    private lateinit var liveKitRoom : Room

    private val TAG = "Stream ViewModel"
    private val WS_URL = "wss://synapse-wj7x7eni.livekit.cloud"


    @SuppressLint("StaticFieldLeak")
    private lateinit var surfaceViewRenderer : SurfaceViewRenderer
    private lateinit var streamer : LocalParticipant
    private var isScreenCaptureRequest = true
    private var mediaProjectionIntent : Intent? = null


    private val _streamStatus = MutableStateFlow<String>(STREAM_STATUS_IDLE)
    val streamStatus : StateFlow<String>
        get() = _streamStatus

    val streamTokenStatus : StateFlow<String>
        get() = streamRepo.streamTokenStatus

    private val _receivedChat = MutableStateFlow<String>("")
    val receivedChat : StateFlow<String>
        get() = _receivedChat


//    val stopStreamOutput : StateFlow<Resource<StopStreamOutput>>
//        get() = streamRepo.stopStreamOutput
//    val startStreamOutput : StateFlow<Resource<StartStreamOutput>>
//        get() = streamRepo.startStreamOutput

    fun init(surfaceViewRenderer: SurfaceViewRenderer, room : Room?){
        if (room != null) {
            this.liveKitRoom = room
            Log.d(TAG, "init: room is not null")
        }
        this.surfaceViewRenderer = surfaceViewRenderer
        liveKitRoom.initVideoRenderer(surfaceViewRenderer)
        activateRoomEventListener()
        activateStartStreamCallListener()
        activateStopStreamCallListener()
    }

    private fun activateStartStreamCallListener(){
        viewModelScope.launch(Dispatchers.Default) {
            streamRepo.startStreamOutput.collect{
                when(it){
                    is Resource.Success ->{
                        Log.d(TAG, "startScreenCapture: startStreamOutput is Success")

                        it.data?.let {startStreamOutput ->
                            val streamerToken = startStreamOutput.streamerRoomToken
                            if (streamerToken != null) {
                                launch(Dispatchers.Main) {
                                    liveKitRoom.connect(WS_URL, streamerToken)
                                    handleStreamer()
                                }
                            }
                            else{
                                Log.d(TAG, "startScreenCapture: returned Stream Token is null")
                            }
                        }
                        if (it.data == null) Log.d(TAG, "startScreenCapture: returned Stream output is null")
                    }
                    is Resource.Loading ->{
                        Log.d(TAG, "startScreenCapture: startStreamOutput is Loading")

                        _streamStatus.emit(STREAM_STATUS_GOING_LIVE)
                    }
                    is Resource.Error ->{
                        Log.d(TAG, "startScreenCapture: startStreamOutput is Error")

                        _streamStatus.emit(STREAM_STATUS_LIVE_ERROR)
                    }
                    is Resource.Idle ->{
                        Log.d(TAG, "startScreenCapture: startStreamOutput is Idle")

                        _streamStatus.emit(STREAM_STATUS_IDLE)
                    }
                }
            }
        }
    }

    private fun activateStopStreamCallListener() {
        viewModelScope.launch(Dispatchers.IO) {
            streamRepo.stopStreamOutput.collect{
                when(it){
                    is Resource.Success ->{
                        Log.d(TAG, "activateStopStreamCallListener: success")

                        liveKitRoom.disconnect()
                        _streamStatus.emit(STREAM_STATUS_STOPPED)
                    }

                    is Resource.Loading ->{
                        Log.d(TAG, "activateStopStreamCallListener: loading")

                        _streamStatus.emit(STREAM_STATUS_STOPPING)
                    }

                    is Resource.Error ->{
                        Log.d(TAG, "activateStopStreamCallListener: error")

                        _streamStatus.emit(STREAM_STATUS_STOP_ERROR)
                    }

                    else ->{
                        Log.d(TAG, "init: else block for stop stream output listner")
                    }
                }
            }
        }
    }

    private fun activateRoomEventListener() {
        viewModelScope.launch {
            liveKitRoom.events.collect { event ->
                when(event){
                    is RoomEvent.TrackSubscribed ->{
                        Log.d(TAG, "activateRoomEventListener: trackSubscribed")

                    }

                    is RoomEvent.DataReceived ->{
                        Log.d(TAG, "activateRoomEventListener: dataReceived")

                        _receivedChat.emit(String(event.data))
                    }

                    is RoomEvent.Connected ->{
                        Log.d(TAG, "activateRoomEventListener: connected")

                        _streamStatus.emit(STREAM_STATUS_LIVE)    // or Loaded based on is viewer or a streamer
                    }

                    is RoomEvent.Disconnected ->{
                        Log.d(TAG, "activateRoomEventListener: disconnected")

                    }

                    is RoomEvent.FailedToConnect ->{
                        Log.d(TAG, "activateRoomEventListener: failedToConnect")

                    }

                    else ->{
                        Log.d("LiveKit - streamViewModel", "activateRoomEventListener: else block")
                    }
                }
            }
        }
    }

    fun startLive(streaminfo : StartStreamInput ){
        isScreenCaptureRequest = false
        viewModelScope.launch(Dispatchers.IO) {
            _streamStatus.emit(STREAM_STATUS_GOING_LIVE)
            streamRepo.startStream(streaminfo)
        }
    }

    fun startScreenCapture(startStreamInput: StartStreamInput, mediaProjectionIntent: Intent?){
        this.isScreenCaptureRequest = true
        this.mediaProjectionIntent = mediaProjectionIntent
        viewModelScope.launch(Dispatchers.IO) {
            _streamStatus.emit(STREAM_STATUS_GOING_LIVE)
            streamRepo.startStream(startStreamInput)
        }
    }

    fun stopStream(){
        viewModelScope.launch(Dispatchers.IO) {
            _streamStatus.emit(STREAM_STATUS_STOPPING)
            streamRepo.stopStream()
        }
    }

    private suspend fun handleStreamer() {
        streamer = liveKitRoom.localParticipant

        if (isScreenCaptureRequest){
            streamer.setScreenShareEnabled(true, mediaProjectionIntent)
        }
        else{
            streamer.setCameraEnabled(true)
            streamer.setMicrophoneEnabled(true)
        }

        while (true){
            if (streamer.videoTrackPublications.size > 0){
                val track = streamer.videoTrackPublications.get(0).second as VideoTrack
                attachVideo(track)
                break
            }
        }
    }

    fun sendChat(msg : String){
        viewModelScope.launch(Dispatchers.IO) {
            var userName = sharedprefUtil.getString(SharedprefUtil.USER_ID)!!
            var profilePicUrl = sharedprefUtil.getString(SharedprefUtil.PROFILE_PIC_URL)!!
            val chatMessage = ChatMessage(userName, msg, profilePicUrl, true)
            streamer.publishData(gson.toJson(chatMessage).toByteArray())
        }
    }

    fun attachVideo(track : VideoTrack){
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        track.addRenderer(surfaceViewRenderer)
    }
}