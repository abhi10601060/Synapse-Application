package com.example.synapse.repo

import android.content.SharedPreferences
import android.util.Log
import com.example.synapse.model.WebSocketMessage
import com.example.synapse.model.WebsocketMessageType
import com.example.synapse.network.socket.SocketClient
import com.example.synapse.network.socket.SocketListener
import com.example.synapse.network.webrtc.WebrtcClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.webrtc.SessionDescription
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
        Log.d(TAG, "onSocketMessage: $message")
        val wsMessage = gson.fromJson(message, JsonObject::class.java)
        val messageType = wsMessage.get("type").asInt
        val userName = wsMessage.get("user").asString
        val target = wsMessage.get("target").asString
        val data = wsMessage.get("data").asString
        when(messageType){
            WebsocketMessageType.ANSWER ->{
                val answerSdp = SessionDescription(SessionDescription.Type.ANSWER, data)
                webrtcClient.useAnswer(answerSdp)
            }
            WebsocketMessageType.OFFER ->{
                val offerSdp = SessionDescription(SessionDescription.Type.OFFER, data)
                webrtcClient.useOffer(offerSdp, fun(answer : SessionDescription){
                    val msg =WebSocketMessage(WebsocketMessageType.ANSWER, user = target, target= userName, data = answer)
                    socketClient.sendMessage(gson.toJson(msg))
                })
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