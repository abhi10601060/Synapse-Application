package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class UpdateBioInput(
    @SerializedName("bio") val bio : String
)
