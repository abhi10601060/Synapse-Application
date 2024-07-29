package com.example.synapse.network

import com.example.synapse.model.req.StopStreamInput
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.model.res.WatchStreamOutput
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.StopStreamOutput
import com.example.synapse.model.res.StartStreamOutput
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface SynapseService {

    @POST("stream/start")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun startStream(
        @Header("Authentication-Token") token: String,
        @Body startStreamInput: StartStreamInput
    ): Response<StartStreamOutput>

    @GET("stream/join/{room_name}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun joinStream(
        @Header("Authentication-Token") token: String,
        @Path("room_name") roomName : String
    ) : Response<WatchStreamOutput>

    @POST("stream/stop")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun stopStream(
        @Header("Authentication-Token") token: String,
        @Body stopStreamInput: StopStreamInput
    ) : Response<StopStreamOutput>


    @GET("stream/all")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAllActiveStreams(
        @Header("Authentication-Token") token: String
    ) : Response<AllActiveStreamOutput>

}