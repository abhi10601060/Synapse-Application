package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class AuthenticationOutput(
    @SerializedName("message") val message : String,
    @SerializedName("token") val token : String
)

