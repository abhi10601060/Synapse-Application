package com.example.synapse.model.res

import com.example.synapse.model.data.Subscription
import com.google.gson.annotations.SerializedName

data class SubscriptionsOutput(
    @SerializedName("message") val message : String,
    @SerializedName("subscribers") val subscriptions : List<Subscription>
)
