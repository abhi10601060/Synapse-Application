package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.res.StartStreamOutput
import com.example.synapse.model.res.WatchStreamOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

const val DUMMY_VIEWER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6InNodWJoYW0iLCJleHAiOjE3MjM4NTI3NzB9.nBatlO1vWy-yfhkE_4NpR3iCuwyvBm-uR93W8BLwsEQ"

class WatchStreamRepo @Inject constructor(
    private val synapseService: SynapseService,
    private val sharedprefUtil: SharedprefUtil,
    private val gson: Gson
) {

    private val _watchStreamOutput = MutableStateFlow<Resource<WatchStreamOutput>>(Resource.Idle())
    val watchStreamOutput : StateFlow<Resource<WatchStreamOutput>>
        get() = _watchStreamOutput

    suspend fun watchStream(streamName : String){
        _watchStreamOutput.emit(Resource.Loading())
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        token = DUMMY_VIEWER_TOKEN
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _watchStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.joinStream(token, streamName)
        val resource = handleWatchStreamResponse(res)
        _watchStreamOutput.emit(resource)
    }

    fun handleWatchStreamResponse(res : Response<WatchStreamOutput>) : Resource<WatchStreamOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success<WatchStreamOutput>(data = res.body()!!)
        }
        Log.d(TAG, "handleWatchStreamResponse: error in start stream body is null")
        return Resource.Error<WatchStreamOutput>(message = "error in start stream body is null")
    }
}