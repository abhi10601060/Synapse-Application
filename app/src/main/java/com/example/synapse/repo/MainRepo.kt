package com.example.synapse.repo

import android.content.SharedPreferences
import android.util.Log
import com.example.synapse.model.WebsocketMessageType
import com.example.synapse.network.socket.SocketClient
import com.example.synapse.network.socket.SocketListener
import com.example.synapse.network.webrtc.WebrtcClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val socketClient: SocketClient,
    private val webrtcClient: WebrtcClient,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SocketListener {


    //********************************************** Web Socket *****************************************************
    private val TAG = "MAIN_REPO"

    override fun onSocketMessage(message: String) {
        Log.d(TAG, "onSocketMessage: ")
        val wsMessage = gson.fromJson(message, JsonObject::class.java)
        val messageType = wsMessage.get("type").asInt
        when(messageType){
            WebsocketMessageType.ANSWER ->{
                // TODO: Will always be a accepted connection sdp from streamer
            }
            WebsocketMessageType.OFFER ->{
                // TODO: Will always be a watch stream request sdp from viewer
            }
            WebsocketMessageType.CLOSE_STREAM ->{

            }
            WebsocketMessageType.CHAT ->{

            }
            else -> {
                Log.d(TAG, "onSocketMessage: else block")
            }
        }

    }

    override fun onSocketOpen() {
        Log.d(TAG, "onSocketOpen: ")
    }

    override fun onSocketClosed() {
        Log.d(TAG, "onSocketClosed: ")
    }

    suspend fun watchStream(streamId : String){
        val token = sharedPreferences.getString("token", null)
        socketClient.createWatchConnection(streamId, token!!, this)
    }

    suspend fun startStream(){
        val token = sharedPreferences.getString("token", null)
        socketClient.createStreamConnection(token!!, this)
    }

}