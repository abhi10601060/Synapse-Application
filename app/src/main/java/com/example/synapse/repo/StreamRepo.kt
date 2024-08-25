package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.req.StopStreamInput
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.StopStreamOutput
import com.example.synapse.model.res.StartStreamOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

const val TAG = "StreamRepo"

const val STREAM_TOKEN_STATUS_IDLE = "idle"
const val STREAM_TOKEN_STATUS_CREATED = "created"
const val DUMMY_STREAMER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6IkFiaGkiLCJleHAiOjE3MjM4NTI4MDZ9.0ZkGhdsZhS_0HKzg_qWmzlsUKTkCVJlPP_olOk9vlco"


class StreamRepo @Inject constructor(
    private val synapseService: SynapseService,
    private val gson: Gson,
    private val sharedprefUtil: SharedprefUtil
) {
    private val _streamTokenStatus = MutableStateFlow<String>(STREAM_TOKEN_STATUS_IDLE)
    val streamTokenStatus : StateFlow<String>
        get() = _streamTokenStatus

    private val _startStreamOutput = MutableStateFlow<Resource<StartStreamOutput>>(Resource.Idle())
    val startStreamOutput : StateFlow<Resource<StartStreamOutput>>
        get() = _startStreamOutput

    private val _stopStreamOutput = MutableStateFlow<Resource<StopStreamOutput>>(Resource.Idle())
    val stopStreamOutput : StateFlow<Resource<StopStreamOutput>>
        get() = _stopStreamOutput

    private val _allActiveStreams = MutableStateFlow<Resource<AllActiveStreamOutput>>(Resource.Idle())
    val allActiveStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = _allActiveStreams

    suspend fun startStream(streamInfo : StartStreamInput){
        _startStreamOutput.emit(Resource.Loading())
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_STREAMER_TOKEN
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _startStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }

        Log.d(TAG, "startStream json input: ${gson.toJson(streamInfo)}")
        val res = synapseService.startStream(token, streamInfo)
        val resource = handleStartStreamResponse(res)
        _startStreamOutput.emit(resource)

        when(resource){
            is Resource.Success ->{
                _streamTokenStatus.emit(STREAM_TOKEN_STATUS_CREATED)
                resource.data?.let { sharedprefUtil.putString(SharedprefUtil.ACTIVE_STREAM_KEY, it.streamId) }
            }
            is Resource.Error ->{
                _streamTokenStatus.emit(STREAM_TOKEN_STATUS_IDLE)
            }
            else -> {
                Log.d(TAG, "startStream: Else block...")
            }
        }
    }

     private fun handleStartStreamResponse(res : Response<StartStreamOutput>) : Resource<StartStreamOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success<StartStreamOutput>(data = res.body()!!)
        }
         Log.d(TAG, "handleStartStreamResponse: error in start stream body is null")
        return Resource.Error<StartStreamOutput>(message = "error in start stream body is null")
    }

    suspend fun stopStream(){
        _stopStreamOutput.emit(Resource.Loading())
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_STREAMER_TOKEN
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _stopStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val roomName = sharedprefUtil.getString(SharedprefUtil.ACTIVE_STREAM_KEY)
        if (roomName == null){
            Log.d("TAG", "closeStream: room name is Empty")
            _stopStreamOutput.emit(Resource.Error(message = "Room name didn't found"))
            return
        }

        val res = synapseService.stopStream(token, StopStreamInput(roomName = roomName))
        val resource = handleCloseStream(res)
        _stopStreamOutput.emit(resource)

        when(resource){
            is Resource.Success ->{
                _streamTokenStatus.emit(STREAM_TOKEN_STATUS_IDLE)
                sharedprefUtil.deleteString(SharedprefUtil.ACTIVE_STREAM_KEY)
            }
            else ->{
                Log.d(TAG, "startStream: Else block...")
            }
        }
    }

    private fun handleCloseStream(res : Response<StopStreamOutput>) : Resource<StopStreamOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleCloseStream: error in close stream body is null")
        return Resource.Error(message = "error in start stream body is null")
    }
}