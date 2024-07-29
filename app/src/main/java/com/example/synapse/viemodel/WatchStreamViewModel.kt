package com.example.synapse.viemodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.network.Resource
import com.example.synapse.repo.WatchStreamRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import livekit.org.webrtc.RendererCommon
import javax.inject.Inject


const val STREAM_STATUS_LOADING = "loading"
const val STREAM_STATUS_LOADED = "loaded"
const val STREAM_STATUS_WATCH_ERROR = "watch_error"
const val STREAM_STATUS_CLOSING = "closing"
const val STREAM_STATUS_CLOSED = "closed"
const val STREAM_STATUS_CLOSE_ERROR = "clos_error"

@HiltViewModel
class WatchStreamViewModel @Inject constructor(
    private val watchStreamRepo: WatchStreamRepo
) : ViewModel() {

    val TAG = "WatchStreamViewModel"
    private val WS_URL = "wss://synapse-wj7x7eni.livekit.cloud"

    @SuppressLint("StaticFieldLeak")
    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var liveKitRoom: Room

    private val _streamStatus = MutableStateFlow<String>(STREAM_STATUS_IDLE)
    val streamStatus : StateFlow<String>
        get() = _streamStatus

    fun init(surfaceViewRenderer: SurfaceViewRenderer, livekitRoom : Room){
        this.surfaceViewRenderer = surfaceViewRenderer
        this.liveKitRoom = livekitRoom
        activateWatchStreamCallListener()
        activateRoomEventListener()
    }

    private fun activateWatchStreamCallListener(){
        viewModelScope.launch(Dispatchers.Default) {
            watchStreamRepo.watchStreamOutput.collect{
                when(it){
                    is Resource.Success ->{
                        Log.d(TAG, "watchStream: watchStreamOutput is Success")

                        it.data?.let {startStreamOutput ->
                            val viewerToken = startStreamOutput.viewerRoomToken
                            if (viewerToken != null) {
                                liveKitRoom.connect(WS_URL, viewerToken)

                            }
                            Log.d(TAG, "watchStream: returned Stream Token is null")
                        }
                        Log.d(TAG, "watchStream: returned Stream output is null")
                    }
                    is Resource.Loading ->{
                        Log.d(TAG, "watchStream: watchStreamOutput is Loading")

                        _streamStatus.emit(STREAM_STATUS_GOING_LIVE)
                    }
                    is Resource.Error ->{
                        Log.d(TAG, "watchStream: watchStreamOutput is Error")

                        _streamStatus.emit(STREAM_STATUS_LIVE_ERROR)
                    }
                    is Resource.Idle ->{
                        Log.d(TAG, "watchStream: watchStreamOutput is Idle")

                        _streamStatus.emit(STREAM_STATUS_IDLE)
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

                        val track = event.track
                        if (track is VideoTrack){
                            Log.d(TAG, "onTrack : track is videoTrack")
                            attachVideo(track)
                            _streamStatus.emit(STREAM_STATUS_LOADED)
                        }
                        else{
                            Log.d(TAG, "onTrack : track is not videoTrack")
                        }
                    }

                    is RoomEvent.DataReceived ->{
                        Log.d(TAG, "activateRoomEventListener: dataReceived")

                    }

                    is RoomEvent.Connected ->{
                        Log.d(TAG, "activateRoomEventListener: connected")

                        _streamStatus.emit(STREAM_STATUS_LOADING)
                    }

                    is RoomEvent.Disconnected ->{
                        Log.d(TAG, "activateRoomEventListener: disconnected")

                    }

                    is RoomEvent.FailedToConnect ->{
                        Log.d(TAG, "activateRoomEventListener: failedToConnect")

                    }

                    else ->{
                        Log.d("LiveKit - watchStream viewModel", "activateRoomEventListener: else block")
                    }
                }
            }
        }
    }

    fun watchStream(streamName : String){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.watchStream(streamName)
        }
    }

    fun closeStream(){
        viewModelScope.launch(Dispatchers.IO) {
            liveKitRoom.disconnect()
            _streamStatus.emit(STREAM_STATUS_CLOSED)
        }
    }

    fun attachVideo(track : VideoTrack){
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        track.addRenderer(surfaceViewRenderer)
    }
}