package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class AllActiveStreamOutput(
    @SerializedName("rooms")
    val streams : List<Stream>
)