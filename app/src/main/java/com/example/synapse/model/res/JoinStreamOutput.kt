package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class JoinStreamOutput(
    @SerializedName("message")
    val message : String,
    @SerializedName("token")
    val viewerRoomToken : String
)