package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class AuthenticationInput(
    @SerializedName("id") val id : String,
    @SerializedName("password") val password : String
)