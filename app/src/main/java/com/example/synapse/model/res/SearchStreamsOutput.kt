package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class SearchStreamsOutput (
    @SerializedName("streams") val streams : List<Stream>
)
