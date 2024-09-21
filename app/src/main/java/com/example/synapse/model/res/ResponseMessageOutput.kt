package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class ResponseMessageOutput(
    @SerializedName("message") val message : String
)