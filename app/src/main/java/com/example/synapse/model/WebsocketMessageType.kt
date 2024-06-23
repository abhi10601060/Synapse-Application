package com.example.synapse.model

class WebsocketMessageType {
    companion object{
        val OFFER = 1
        val ANSWER = 2
        val CHAT = 0
        val CLOSE_STREAM = -1
    }
}