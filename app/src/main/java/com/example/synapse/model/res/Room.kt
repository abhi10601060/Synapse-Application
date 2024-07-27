package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class Room(
    @SerializedName("sid")
    val sessionId : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("creation_time")
    val creationTime : Long
)
