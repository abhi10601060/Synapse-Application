package com.example.synapse.di

import android.content.Context
import android.content.SharedPreferences
import com.example.synapse.model.DITest
import com.example.synapse.network.http.SynapseService
import com.example.synapse.network.socket.SocketClient
import com.example.synapse.network.webrtc.WebrtcClient
import com.example.synapse.repo.MainRepo
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
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
    fun getSocketClient() : SocketClient {
        return SocketClient()
    }

    @Provides
    @Singleton
    fun getWebRtcClient(@ApplicationContext context : Context) : WebrtcClient {
        return WebrtcClient(context)
    }
    @Provides
    @Singleton
    fun getGson() : Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun getDb(@ApplicationContext context: Context) : SharedPreferences {
        return context.getSharedPreferences("userDB" , Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun getSynapseService() : SynapseService{
        val retroInstance = Retrofit.Builder()
            .baseUrl("http://ec2-65-0-127-171.ap-south-1.compute.amazonaws.com:8010/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retroInstance.create(SynapseService::class.java)
    }

    @Provides
    @Singleton
    fun getMainRepo(socketClient: SocketClient, webrtcClient: WebrtcClient, gson: Gson, userDB : SharedPreferences, synapseService: SynapseService) : MainRepo{
        return  MainRepo(socketClient, webrtcClient, userDB, gson, synapseService)
    }

}