package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class AllActiveStreamOutput(
    @SerializedName("message") val message : String,
    @SerializedName("streams") val streams : List<Stream>
)