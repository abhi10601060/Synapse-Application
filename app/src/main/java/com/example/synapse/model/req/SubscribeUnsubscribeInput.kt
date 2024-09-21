package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class SubscribeUnsubscribeInput(
    @SerializedName("streamerId") val streamerName : String
)
