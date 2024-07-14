package com.example.synapse.repo

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.synapse.model.Stream
import com.example.synapse.model.WebSocketMessage
import com.example.synapse.model.WebsocketMessageType
import com.example.synapse.network.http.SynapseService
import com.example.synapse.network.socket.SocketClient
import com.example.synapse.network.socket.SocketListener
import com.example.synapse.network.webrtc.MyPeerObserver
import com.example.synapse.network.webrtc.WebrtcClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import retrofit2.Response
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val socketClient: SocketClient,
    private val webrtcClient: WebrtcClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val synapseService: SynapseService
) : SocketListener {


    //********************************************** Web Socket Listeners *****************************************************
    private val TAG = "MAIN_REPO"
    private val VIEWER = "Abhi"
    private val STREAMER = "shubham"
    private val VIEWER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6IkFiaGkiLCJleHAiOjE3MjM4NTI4MDZ9.0ZkGhdsZhS_0HKzg_qWmzlsUKTkCVJlPP_olOk9vlco"
    private val STREAMER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6InNodWJoYW0iLCJleHAiOjE3MjM4NTI3NzB9.nBatlO1vWy-yfhkE_4NpR3iCuwyvBm-uR93W8BLwsEQ"

    private lateinit var permissionIntent : Intent
    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var peerConnectionListener: PeerConnectionListener
    private var iAmStreamer = false
    private var isRevived = false
    private lateinit var currentStreamPid : String
    private lateinit var currentWatchingStreamPid : String

    override fun onSocketOpen() {
        if (iAmStreamer && !isRevived){
            webrtcClient.startScreenCaptureStream(permissionIntent, surfaceViewRenderer)
            socketClient.sendMessage(gson.toJson(WebSocketMessage(
                type = 5,
                user = STREAMER,
                target = VIEWER,
                data = "Hi stream started"
            )));
        }
        else{
            if (!isRevived) createViewerSidePeerConnection(STREAMER)
        }
        socketClient.sendMessage("1")
    }

    override fun onSocketMessage(message: String) {
        Log.d(TAG, "onSocketMessage: $message")
        val wsMessage = gson.fromJson(message, JsonObject::class.java)
        val messageType = wsMessage.get("type").asInt
//        val userName = wsMessage.get("user").asString
//        val target = wsMessage.get("target").asString
        val data = wsMessage.get("data").asString
        when(messageType){
            WebsocketMessageType.ANSWER ->{
                val answerSdp = SessionDescription(SessionDescription.Type.ANSWER, data)
                webrtcClient.useAnswerFromStreamer(answerSdp)
                webrtcClient.addIceCandidateToStreamerConn(null)
            }
            WebsocketMessageType.OFFER ->{
                val offerSdp = SessionDescription(SessionDescription.Type.OFFER, data)
                webrtcClient.useOfferFromViewer(viewer = VIEWER,
                    peerConnObserver = getStreamerSidePeerObserver(VIEWER),
                    offer = offerSdp,
                    answerListener =  fun(answer : SessionDescription){
                    val msg =WebSocketMessage(WebsocketMessageType.ANSWER, user = STREAMER, target= VIEWER, data = answer.description)
                    socketClient.sendMessage(gson.toJson(msg))
                })
            }
            WebsocketMessageType.CLOSE_STREAM ->{

            }
            WebsocketMessageType.CHAT ->{

            }
            WebsocketMessageType.ICE ->{
                try {
                    val receivedIce = gson.fromJson(data, IceCandidate::class.java)

                    if (iAmStreamer){
                        webrtcClient.addIceCandidateToViewersConn(VIEWER, receivedIce)
                    }
                    else{
                        webrtcClient.addIceCandidateToStreamerConn(receivedIce)
                    }
                }catch (e : Exception){
                    Log.d(TAG, "onSocketMessage: error in ice json")
                }
                
            }
            WebsocketMessageType.PID ->{
                currentStreamPid = data
            }
            else -> {
                Log.d(TAG, "onSocketMessage: else block")
            }
        }

    }

    override fun onSocketClosed() {
        Log.d(TAG, "onSocketClosed: ")
    }

    override fun onFailure() {

        isRevived = true
        if (iAmStreamer && !isConnecting){
            Log.d(TAG, "onFailure: Revivie req sent")
            socketClient.reviveStreamerConnection(currentStreamPid, STREAMER_TOKEN, this)
        }else if(!isConnecting){
            Log.d(TAG, "onFailure: Revivie req sent")
            socketClient.createWsConnForViewer(currentWatchingStreamPid, VIEWER_TOKEN,this)
        }
    }


// ****************************************************** For Streamer **********************************************8


   fun startStream(permissionIntent : Intent, surfaceViewRenderer: SurfaceViewRenderer){
       isRevived = false
       this.permissionIntent = permissionIntent
       this.surfaceViewRenderer = surfaceViewRenderer
       val token = STREAMER_TOKEN
       iAmStreamer = true
       socketClient.createStreamerConnectionToStartStream(token, this)
    }

    private fun getStreamerSidePeerObserver(viewer :String) : MyPeerObserver{
        return object : MyPeerObserver(viewer){
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    webrtcClient.addIceCandidateToViewersConn(viewer, p0)
                    val wsMessage = WebSocketMessage(
                        type = WebsocketMessageType.ICE,
                        user = STREAMER,
                        target = VIEWER,
                        data = it
                    )
                    socketClient.sendMessage(gson.toJson(wsMessage))
                }
                Log.d(TAG, "onIceCandidate: $viewer : ${p0.toString()}")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d("TAG", "onConnectionChange: $newState")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED){
                    peerConnectionListener?.onConnected()
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                Log.d("TAG", "onAddStream: Stream added by streamer")
//                p0?.let { listener?.onRemoteStreamAdded(it) }
            }
        }
    }

    fun closeStream(){
//        socketClient.onDestroy()
//        webrtcClient.onDestroy()
    }


// ****************************************************** For Viewer **************************************************************


    fun watchStream(streamId : String, peerConnectionListener: PeerConnectionListener){
        isRevived = false
        currentWatchingStreamPid = streamId
        this.peerConnectionListener = peerConnectionListener
        val token = VIEWER_TOKEN
        iAmStreamer = false
        socketClient.createWsConnForViewer(streamId, token, this)
    }

    private fun createViewerSidePeerConnection(streamer: String){
        webrtcClient.createViewerToStreamerConnection(getViewerSidePeerObserver(STREAMER))
        webrtcClient.createOfferToStreamer{ offerSdp ->
            val wsMessage = WebSocketMessage(
                type = WebsocketMessageType.OFFER,
                user = VIEWER,
                target = STREAMER,
                data = offerSdp.description
            )
            socketClient.sendMessage(gson.toJson(wsMessage))
        }
    }
    private var isConnecting = false
    private fun getViewerSidePeerObserver(streamer : String) : MyPeerObserver{
        return object : MyPeerObserver(streamer){
            override fun onIceCandidate(p0: IceCandidate?) {
                p0?.let {
                    webrtcClient.addIceCandidateToStreamerConn(p0)
                    val wsMessage = WebSocketMessage(
                        type = WebsocketMessageType.ICE,
                        user = sharedPreferences.getString("user" , "null")!!,
                        target = streamer,
                        data = it
                    )
                    socketClient.sendMessage(gson.toJson(wsMessage))
                }
                Log.d(TAG, "onIceCandidate: $streamer : ${p0.toString()}")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d("TAG", "onConnectionChange: $newState")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED){
                    peerConnectionListener?.onConnected()
                }
                if(newState==PeerConnection.PeerConnectionState.CONNECTING){
                    isConnecting = true
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                Log.d("TAG", "onAddStream: $p0")
                p0?.let { peerConnectionListener?.onStreamAdded(it) }
            }
        }
    }

    fun leaveStream(){
//        socketClient.onDestroy()
//        webrtcClient.onDestroy()
    }


    private val _streams = MutableLiveData<List<Stream>>()
    val streams : LiveData<List<Stream>>
        get() = _streams
    suspend fun getAllStreams(){
        val res = synapseService.getAllStreams()
        handleStream(res)
    }

    private fun handleStream(response : Response<List<Stream>>) {
        Log.d(TAG, "handleStream: res = ${response.body()}, raw = ${response.toString()}")
        if (response.body() != null && response.isSuccessful){
            Log.d(TAG, "handleStream: Streams found")
            _streams.postValue(response.body())
            return
        }
        Log.d(TAG, "handleStream: Streams Not found")
    }

}

interface PeerConnectionListener{
    fun onConnected()
    fun onStreamAdded(stream : MediaStream)
}