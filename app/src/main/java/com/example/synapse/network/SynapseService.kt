package com.example.synapse.network

import com.example.synapse.model.req.CloseStreamInput
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.model.res.JoinRoomOutput
import com.example.synapse.model.res.AllActiveRoomsOutput
import com.example.synapse.model.res.CloseStreamOutput
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
    ) : Response<JoinRoomOutput>

    @POST("stream/stop")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun closeStream(
        @Header("Authentication-Token") token: String,
        @Body closeStreamInput: CloseStreamInput
    ) : Response<CloseStreamOutput>


    @GET("stream/all")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAllActiveStreams(
        @Header("Authentication-Token") token: String
    ) : Response<AllActiveRoomsOutput>

}