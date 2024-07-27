package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class StartStreamInput(
    @SerializedName("room")
    val roomName : String
)
