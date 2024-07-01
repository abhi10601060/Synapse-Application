package com.example.synapse.repo

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.synapse.model.WebSocketMessage
import com.example.synapse.model.WebsocketMessageType
import com.example.synapse.network.socket.SocketClient
import com.example.synapse.network.socket.SocketListener
import com.example.synapse.network.webrtc.MyPeerObserver
import com.example.synapse.network.webrtc.WebrtcClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val socketClient: SocketClient,
    private val webrtcClient: WebrtcClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SocketListener {


    //********************************************** Web Socket Listeners *****************************************************
    private val TAG = "MAIN_REPO"

    private lateinit var permissionIntent : Intent
    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private var isStreamer = false

    override fun onSocketOpen() {
        Log.d(TAG, "onSocketOpen: ")
        // TODO: Start the peer connection process from the viewer Side or streamer side
        if (isStreamer){
            webrtcClient.startScreenCaptureStream(permissionIntent, surfaceViewRenderer)
        }
        else{
            createViewerSidePeerConnection("streamer_name")
        }
    }

    override fun onSocketMessage(message: String) {
        Log.d(TAG, "onSocketMessage: $message")
        val wsMessage = gson.fromJson(message, JsonObject::class.java)
        val messageType = wsMessage.get("type").asInt
        val userName = wsMessage.get("user").asString
        val target = wsMessage.get("target").asString
        val data = wsMessage.get("data").asString
        when(messageType){
            WebsocketMessageType.ANSWER ->{
                val answerSdp = SessionDescription(SessionDescription.Type.ANSWER, data)
                webrtcClient.useAnswerFromStreamer(answerSdp)
            }
            WebsocketMessageType.OFFER ->{
                val offerSdp = SessionDescription(SessionDescription.Type.OFFER, data)
                webrtcClient.useOfferFromViewer(viewer = userName,
                    peerConnObserver = getStreamerSidePeerObserver(userName),
                    offer = offerSdp,
                    answerListener =  fun(answer : SessionDescription){
                    val msg =WebSocketMessage(WebsocketMessageType.ANSWER, user = target, target= userName, data = answer)
                    socketClient.sendMessage(gson.toJson(msg))
                })
            }
            WebsocketMessageType.CLOSE_STREAM ->{

            }
            WebsocketMessageType.CHAT ->{

            }
            WebsocketMessageType.ICE ->{

            }
            else -> {
                Log.d(TAG, "onSocketMessage: else block")
            }
        }

    }

    override fun onSocketClosed() {
        Log.d(TAG, "onSocketClosed: ")
    }


// ****************************************************** For Streamer **********************************************8


    suspend fun startStream(permissionIntent : Intent, surfaceViewRenderer: SurfaceViewRenderer){
        this.permissionIntent = permissionIntent
        this.surfaceViewRenderer = surfaceViewRenderer
        val token = sharedPreferences.getString("token", null)
        socketClient.createStreamerConnectionToStartStream(token!!, this)
    }

    private fun getStreamerSidePeerObserver(viewer :String) : MyPeerObserver{
        return object : MyPeerObserver(viewer){
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webrtcClient.addIceCandidateToViewersConn(viewer, p0)
                }
                Log.d(TAG, "onIceCandidate: $viewer : ${p0.toString()}")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                Log.d("TAG", "onConnectionChange: $newState")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED){
//                    listener?.onConnectionConnected()
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                Log.d("TAG", "onAddStream: $p0")
//                p0?.let { listener?.onRemoteStreamAdded(it) }
            }
        }
    }


// ****************************************************** For Viewer **************************************************************


    suspend fun watchStream(streamId : String){
        val token = sharedPreferences.getString("token", null)
        socketClient.createWsConnForViewer(streamId, token!!, this)
    }

    private fun createViewerSidePeerConnection(streamer: String){
        webrtcClient.createViewerToStreamerConnection(getViewerSidePeerObserver("streamerName"))
        webrtcClient.createOfferToStreamer{ offerSdp ->
            val wsMessage = WebSocketMessage(
                type = WebsocketMessageType.OFFER,
                user = sharedPreferences.getString("userName", null)!!,
                target = streamer,
                data = gson.toJson(offerSdp)
            )
            socketClient.sendMessage(gson.toJson(wsMessage))
        }
    }

    private fun getViewerSidePeerObserver(streamer : String) : MyPeerObserver{
        return object : MyPeerObserver(streamer){
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webrtcClient.addIceCandidateToStreamerConn(p0)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                Log.d("TAG", "onConnectionChange: $newState")
                if (newState == PeerConnection.PeerConnectionState.CONNECTED){
//                    listener?.onConnectionConnected()
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                Log.d("TAG", "onAddStream: $p0")
//                p0?.let { listener?.onRemoteStreamAdded(it) }
            }
        }
    }

}