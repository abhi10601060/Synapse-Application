package com.example.synapse.model.res

import com.example.synapse.model.data.ProfileDetails
import com.google.gson.annotations.SerializedName

data class SearchUserOutput(
    @SerializedName("users") val users : List<ProfileDetails>
)
