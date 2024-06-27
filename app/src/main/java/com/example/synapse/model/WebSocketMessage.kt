package com.example.synapse.model

data class WebSocketMessage(
    val type : Int,
    val user : String,
    val target : String,
    val data : Any?
)
