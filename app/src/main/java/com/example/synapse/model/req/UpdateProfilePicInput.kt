package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class UpdateProfilePicInput(
    @SerializedName("profilePicture") val profilePictureB64 : String
)