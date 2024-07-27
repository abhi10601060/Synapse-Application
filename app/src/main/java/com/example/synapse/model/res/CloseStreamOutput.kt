package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class CloseStreamOutput(
    @SerializedName("message")
    val message : String
)
