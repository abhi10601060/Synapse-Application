package com.example.synapse.network

import com.example.synapse.model.req.AuthenticationInput
import com.example.synapse.model.res.AuthenticationOutput
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    @POST("login")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun login(
        @Body authInput : AuthenticationInput
    ) : Response<AuthenticationOutput>

    @POST("signup")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun signup(
        @Body authInput : AuthenticationInput
    ): Response<AuthenticationOutput>

}