package com.example.synapse.model.data

import com.google.gson.annotations.SerializedName

data class ProfileDetails(
    @SerializedName("id") val userName : String,
    @SerializedName("bio") val bio : String,
    @SerializedName("profilePictureUrl") val profilePictureUrl : String,
    @SerializedName("totalSubs") val totalSubs : String,
    @SerializedName("totalStreams") val totalStreams : String,
    @SerializedName("createdOn") val createdOn : String,
    @SerializedName("status") val streamStatus : String,
)
