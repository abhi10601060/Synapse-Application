package com.example.synapse.model.req

import com.google.gson.annotations.SerializedName

data class StartStreamInput(
    @SerializedName("title") val title : String,
    @SerializedName("desc") val desc : String="",
    @SerializedName("tags") val tags : String ="",
    @SerializedName("thumbnail") val thumbnail : String="",
    @SerializedName("tosave") val toSave : Boolean = false
)
