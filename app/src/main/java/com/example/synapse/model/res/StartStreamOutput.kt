package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class StartStreamOutput(
    @SerializedName("message")
    val message : String?,
    @SerializedName("token")
    val streamerRoomToken : String?,
    @SerializedName("streamId")
    val streamId : String
)