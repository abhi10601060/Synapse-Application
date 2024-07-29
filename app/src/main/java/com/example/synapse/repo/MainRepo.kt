package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.Stream
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val synapseService : SynapseService,
    private val sharedprefUtil: SharedprefUtil,
    private val gson: Gson
) {
    private val _activeStreams = MutableStateFlow<Resource<AllActiveStreamOutput>>(Resource.Idle())
    val activeStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = _activeStreams


    suspend fun getAllActiveStreams(){
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _activeStreams.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.getAllActiveStreams(token)
        val resource = handleAllActiveStreams(res)
        _activeStreams.emit(resource)
    }

    private fun handleAllActiveStreams (res : Response<AllActiveStreamOutput>) : Resource<AllActiveStreamOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleAllActiveStreams: error in all active streams body is null")
        return Resource.Error(message = "error in all active streams body is null")
    }
}