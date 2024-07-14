package com.example.synapse.network.http

import com.example.synapse.model.Stream
import retrofit2.Response

import retrofit2.http.GET

interface SynapseService {

    @GET("stream/get/all")
    suspend fun getAllStreams() : Response<List<Stream>>
}