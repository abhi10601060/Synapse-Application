package com.example.synapse.model.res

import com.example.synapse.model.data.ProfileDetails
import com.google.gson.annotations.SerializedName

data class ProfileDetailsOutPut(
    @SerializedName("message") val message : String,
    @SerializedName("details") val profileDetails : ProfileDetails
)
