package com.example.synapse.viemodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.ChatMessage
import com.example.synapse.model.req.LikeDislikeInput
import com.example.synapse.model.req.SubscribeUnsubscribeInput
import com.example.synapse.model.res.ResponseMessageOutput
import com.example.synapse.network.Resource
import com.example.synapse.repo.WatchStreamRepo
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
    private val watchStreamRepo: WatchStreamRepo,
    private val gson: Gson,
    private val sharedprefUtil: SharedprefUtil
) : ViewModel() {

    val TAG = "WatchStreamViewModel"
    private val WS_URL = "wss://synapse-wj7x7eni.livekit.cloud"

    @SuppressLint("StaticFieldLeak")
    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var liveKitRoom: Room
    private lateinit var viewer : LocalParticipant

    private val _streamStatus = MutableStateFlow<String>(STREAM_STATUS_IDLE)
    val streamStatus : StateFlow<String>
        get() = _streamStatus

    private val _receivedChat = MutableStateFlow<String>("")
    val receivedChat : StateFlow<String>
        get() = _receivedChat

    fun init(surfaceViewRenderer: SurfaceViewRenderer, room : Room?){
        this.surfaceViewRenderer = surfaceViewRenderer
        if (room != null) {
            this.liveKitRoom = room
        }
        liveKitRoom.initVideoRenderer(surfaceViewRenderer)
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
                            else{
                                Log.d(TAG, "watchStream: returned Stream Token is null")
                            }
                        }
                        if (it.data == null) Log.d(TAG, "watchStream: returned Stream output is null")
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
                            launch(Dispatchers.Main) {
                                attachVideo(track)
                                val height = event.publication.dimensions?.height
                                val width = event.publication.dimensions?.width

                                Log.d("Livekit", "Remote stream startStream: height : $height and width : $width")
                                _streamStatus.emit(STREAM_STATUS_LOADED)
                            }
                        }
                        else{
                            Log.d(TAG, "onTrack : track is not videoTrack")
                        }
                    }

                    is RoomEvent.DataReceived ->{
                        Log.d(TAG, "activateRoomEventListener: dataReceived")

                        _receivedChat.emit(String(event.data))
                    }

                    is RoomEvent.Connected ->{
                        Log.d(TAG, "activateRoomEventListener: connected")

                        _streamStatus.emit(STREAM_STATUS_LOADED)
                        viewer = liveKitRoom.localParticipant
                    }

                    is RoomEvent.Disconnected ->{
                        Log.d(TAG, "activateRoomEventListener: disconnected")

                    }

                    is RoomEvent.FailedToConnect ->{
                        Log.d(TAG, "activateRoomEventListener: failedToConnect")
                        _streamStatus.emit(STREAM_STATUS_LIVE_ERROR)
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
            Log.d(TAG, "watchStream: room state is ${liveKitRoom.state.name}")
            delay(1000)
            watchStreamRepo.watchStream(streamName)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun closeStream(){
        GlobalScope.launch(Dispatchers.Main) {
            liveKitRoom.disconnect()
            liveKitRoom.release()
            _streamStatus.emit(STREAM_STATUS_CLOSED)
            watchStreamRepo.closeStream()
        }
    }

    fun sendChat(msg : String){
        viewModelScope.launch(Dispatchers.IO) {
            var userName = sharedprefUtil.getString(SharedprefUtil.USER_ID)!!
            var profilePicUrl = sharedprefUtil.getString(SharedprefUtil.PROFILE_PIC_URL)!!
            val chatMessage = ChatMessage(userName =  userName, message = msg, profilePicUrl = profilePicUrl, isStreamer = false)
            viewer.publishData(gson.toJson(chatMessage).toByteArray())
        }
    }

    fun attachVideo(track : VideoTrack){
        Log.d(TAG, "attachVideo: called for watch video")
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        track.addRenderer(surfaceViewRenderer)
    }

    //*************************************************************** Stream Actions **********************************************

    val likeStreamOutput : StateFlow<Int>
        get() = watchStreamRepo.likeDisLikeStreamOutput

    val subUnSubStatus : StateFlow<Int>
        get() = watchStreamRepo.subUnSubOutput

    fun likeStream(likeInput: LikeDislikeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.likeStream(likeInput)
        }
    }

    fun removeLikeOfStream(likeDislikeInput: LikeDislikeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.removeLikeOfStream(likeDislikeInput)
        }
    }
    fun dislikeStream(likeDislikeInput: LikeDislikeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.dislikeStream(likeDislikeInput)
        }
    }
    fun removeDislikeOfStream(likeDislikeInput: LikeDislikeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.removeDislikeStream(likeDislikeInput)
        }
    }

    fun subscribeStreamer(subscribeUnsubscribeInput: SubscribeUnsubscribeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.subscribeStreamer(subscribeUnsubscribeInput)
        }
    }
    fun unsubscribeStreamer(subscribeUnsubscribeInput: SubscribeUnsubscribeInput){
        viewModelScope.launch(Dispatchers.IO) {
            watchStreamRepo.unsubscribeStreamer(subscribeUnsubscribeInput)
        }
    }
}