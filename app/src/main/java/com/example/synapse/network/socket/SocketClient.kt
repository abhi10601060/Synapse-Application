package com.example.synapse.network.socket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.LinkedList
import java.util.Queue
import javax.inject.Singleton

@Singleton
class SocketClient {
    private val TAG = "WEBSOCKET"
    private var webSocket : WebSocket? = null
    val okHttpClient = OkHttpClient()

    fun createStreamerConnectionToStartStream(token : String, listener : SocketListener){
        val streamUrl = "ws://ec2-65-0-127-171.ap-south-1.compute.amazonaws.com:8010/stream/start"
        val request = Request.Builder()
            .addHeader("Authentication-Token", token)
            .url(streamUrl)
            .build()
//        webSocket?.close(1000, "already exist...switching")

        webSocket = okHttpClient.newWebSocket(request, getWebSocketListener(listener))
    }

    fun reviveStreamerConnection(streamPid :String, token: String, listener: SocketListener){
        val streamUrl = "ws://ec2-65-0-127-171.ap-south-1.compute.amazonaws.com:8010/stream/start/revive/$streamPid"
        val request = Request.Builder()
            .addHeader("Authentication-Token", token)
            .url(streamUrl)
            .build()
//        webSocket?.close(1000, "already exist...switching")

        webSocket = okHttpClient.newWebSocket(request, getWebSocketListener(listener))
    }

    fun createWsConnForViewer(streamId : String, token : String, listener : SocketListener){
        var watchUrl = "ws://ec2-65-0-127-171.ap-south-1.compute.amazonaws.com:8010/stream/join/$streamId"
        val request = Request.Builder()
            .addHeader("Authentication-Token", token)
            .url(watchUrl)
            .build()
//        webSocket?.close(1000, "already exist...switching")

        webSocket = okHttpClient.newWebSocket(request, getWebSocketListener(listener))
    }

    fun closeConnection(){
        webSocket?.close(1001, "closing manually")
        webSocket = null
    }

    fun isConnected() : Boolean{
        return webSocket != null
    }

    private fun getWebSocketListener(listener: SocketListener) : WebSocketListener{
        return  object: WebSocketListener(){
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d(TAG, "onClosed: code: $code, reason: $reason")
                listener.onSocketClosed()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.d(TAG, "onClosing: code: $code, reason: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d(TAG, "onFailure: ${response?.message}")
                listener.onFailure()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                listener.onSocketMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Log.d(TAG, "onMessage (Bytes Type): $bytes")
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d(TAG, "onOpen: ${response.body}")
                listener.onSocketOpen()
            }
        }
    }

    val q : Queue<String> = LinkedList<String>()
    fun sendMessage(message : String){
        if (!message.equals("1")) q.offer(message)
        while (!q.isEmpty()){
            val res = webSocket?.send(message)
            if (res == null || !res){
                Log.d(TAG, "sendMessage: message could not sent : $message")
                return
            }
            q.poll()
        }
        Log.d(TAG, "sendMessage: Queue is empty...")
    }
}