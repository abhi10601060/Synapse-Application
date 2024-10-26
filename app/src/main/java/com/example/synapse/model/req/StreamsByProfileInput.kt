package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class StreamsByProfileInput(
    @SerializedName("profiles") val profiles : List<String>
)
