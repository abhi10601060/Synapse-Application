package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class LikeDislikeInput(
    @SerializedName("streamId") val streamId : String
)