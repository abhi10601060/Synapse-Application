package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class Stream(
    @SerializedName("id") val streamId : String,
    @SerializedName("userId") val streamerId : String,
    @SerializedName("title") val title : String,
    @SerializedName("desc") val desc : String,
    @SerializedName("tags") val tags : String,
    @SerializedName("status") val status : String,
    @SerializedName("savedStreamUrl") val savedStreamUrl : String,
    @SerializedName("likes") val likes : Int,
    @SerializedName("dislikes") val dislikes : String,
    @SerializedName("thumbNailUrl") val thumbNailUrl : String,
    @SerializedName("createdOn") val createdOn : String,
    @SerializedName("endedOn") val endedOn : String,
)
