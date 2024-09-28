package com.example.synapse.model.data

import com.google.gson.annotations.SerializedName

data class Subscription(
    @SerializedName("id") val id : String,
    @SerializedName("bio") val bio : String,
    @SerializedName("profilePictureUrl") val profilePicUrl : String,
    @SerializedName("totalSubs") val totalSubs : String,
    @SerializedName("totalStreams") val totalStreams : String,
    @SerializedName("createdOn") val createdOn : String,
    @SerializedName("status") val status : String,
)