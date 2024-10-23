package com.example.synapse.network

import com.example.synapse.model.req.LikeDislikeInput
import com.example.synapse.model.req.StopStreamInput
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.model.req.SubscribeUnsubscribeInput
import com.example.synapse.model.req.UpdateBioInput
import com.example.synapse.model.req.UpdateProfilePicInput
import com.example.synapse.model.res.WatchStreamOutput
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.model.res.ResponseMessageOutput
import com.example.synapse.model.res.SearchStreamsOutput
import com.example.synapse.model.res.SearchUserOutput
import com.example.synapse.model.res.StopStreamOutput
import com.example.synapse.model.res.StartStreamOutput
import com.example.synapse.model.res.SubscriptionsOutput
import com.example.synapse.model.res.UpdateProfileOutput
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


    //************************************************ User Management **********************************************************

    @GET("user/details")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getOwnProfileDetails(
        @Header("Authentication-Token") token: String
    ) : Response<ProfileDetailsOutPut>

    @POST("user/update/profile-pic")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun updateProfilePic(
        @Header("Authentication-Token") token: String,
        @Body profilePicture : UpdateProfilePicInput
    ) : Response<UpdateProfileOutput>

    @POST("user/update/bio")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun updateBio(
        @Header("Authentication-Token") token: String,
        @Body bio : UpdateBioInput
    ) : Response<UpdateProfileOutput>

    //************************************************ Stream Actions **********************************************************

    @POST("stream/like")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun likeStream(
        @Header("Authentication-Token") token: String,
        @Body likeInput : LikeDislikeInput
    ) : Response<ResponseMessageOutput>

    @POST("stream/dislike")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun dislikeStream(
        @Header("Authentication-Token") token: String,
        @Body dislikeInput : LikeDislikeInput
    ) : Response<ResponseMessageOutput>

    @POST("stream/remove-like")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun removeLikeOfStream(
        @Header("Authentication-Token") token: String,
        @Body removeLikeInput : LikeDislikeInput
    ) : Response<ResponseMessageOutput>

    @POST("stream/remove-dislike")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun removeDislikeOfStream(
        @Header("Authentication-Token") token: String,
        @Body removeDislikeInput : LikeDislikeInput
    ) : Response<ResponseMessageOutput>

    @POST("user/subscribe")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun subscribeStreamer(
        @Header("Authentication-Token") token: String,
        @Body subscribeUnsubscribeInput: SubscribeUnsubscribeInput
    ) : Response<ResponseMessageOutput>

    @POST("user/unsubscribe")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun unsubscribeStreamer(
        @Header("Authentication-Token") token: String,
        @Body subscribeUnsubscribeInput: SubscribeUnsubscribeInput
    ) : Response<ResponseMessageOutput>


    //************************************************ Subscriptions *****************************************************************

    @GET("user/subscribers")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getAllSubscriptions(
        @Header("Authentication-Token") token: String
    ) : Response<SubscriptionsOutput>


    //************************************************ Search *****************************************************************

    @GET("stream/search/{searchParam}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getSearchedStreams(
        @Header("Authentication-Token") token: String,
        @Path("searchParam") searchParam : String
    ) : Response<SearchStreamsOutput>

    @GET("user/search/{searchParam}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getSearchedUsers(
        @Header("Authentication-Token") token: String,
        @Path("searchParam") searchParam : String
    ) : Response<SearchUserOutput>


}