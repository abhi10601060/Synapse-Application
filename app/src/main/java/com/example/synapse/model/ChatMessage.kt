package com.example.synapse.model

data class ChatMessage(
    val userName : String,
    val message : String,
    val profilePicUrl : String,
    val isStreamer : Boolean
)