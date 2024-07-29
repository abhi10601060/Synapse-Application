package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class WatchStreamOutput(
    @SerializedName("message")
    val message : String,
    @SerializedName("token")
    val viewerRoomToken : String?
)