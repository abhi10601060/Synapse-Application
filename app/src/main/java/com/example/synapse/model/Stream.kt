package com.example.synapse.model

import com.google.gson.annotations.SerializedName

data class Stream(
    @SerializedName("id")
    val id : String,
    @SerializedName("streamer")
    val streamer : String
)
