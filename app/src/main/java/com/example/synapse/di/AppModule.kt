package com.example.synapse.di

import android.content.Context
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.DITest
import com.example.synapse.network.SynapseService
import com.example.synapse.repo.MainRepo
import com.example.synapse.repo.ProfileRepo
import com.example.synapse.repo.StreamRepo
import com.example.synapse.repo.WatchStreamRepo
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun getDiTestModel() : DITest{
        return DITest("test for di")
    }

    @Provides
    @Singleton
    fun getGson() : Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun getRetroInstance() : Retrofit{
        val url = "http://ec2-15-207-117-224.ap-south-1.compute.amazonaws.com:8010/"

        val intercepter = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().apply {
            this.addInterceptor(intercepter)
                // time out setting
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(20,TimeUnit.SECONDS)
                .writeTimeout(25,TimeUnit.SECONDS)

        }.build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun getSynapseService(retrofit : Retrofit) : SynapseService{
        return retrofit.create(SynapseService::class.java)
    }

    @Provides
    @Singleton
    fun getSharedPrefUtil(@ApplicationContext context: Context) : SharedprefUtil{
        return SharedprefUtil(context)
    }

    @Provides
    @Singleton
    fun getStreamRepository(synapseService: SynapseService, gson: Gson, sharedprefUtil: SharedprefUtil) : StreamRepo{
        return StreamRepo(synapseService, gson, sharedprefUtil)
    }

    @Provides
    @Singleton
    fun getMainRepository(synapseService: SynapseService, gson: Gson, sharedprefUtil: SharedprefUtil) : MainRepo{
        return MainRepo(synapseService, sharedprefUtil, gson)
    }

    @Provides
    @Singleton
    fun getWatchStreamRepository(synapseService: SynapseService, sharedprefUtil: SharedprefUtil, gson: Gson) : WatchStreamRepo{
        return WatchStreamRepo(synapseService, sharedprefUtil, gson)
    }

    @Provides
    @Singleton
    fun getProfileRepository(synapseService: SynapseService, sharedprefUtil: SharedprefUtil) : ProfileRepo{
        return ProfileRepo(synapseService, sharedprefUtil)
    }
}