package com.example.synapse.model.res

import com.google.gson.annotations.SerializedName

data class AllActiveRoomsOutput(
    @SerializedName("rooms")
    val rooms : List<Room>
)