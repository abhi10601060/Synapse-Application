package com.example.synapse.network.socket

interface SocketListener {
    fun onSocketMessage(message : String)
    fun onSocketOpen()
    fun onSocketClosed()
}