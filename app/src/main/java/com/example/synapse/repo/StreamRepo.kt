package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.req.CloseStreamInput
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.model.res.AllActiveRoomsOutput
import com.example.synapse.model.res.CloseStreamOutput
import com.example.synapse.model.res.StartStreamOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

const val TAG = "StreamRepo"

const val STREAM_STATUS_IDLE = "idle"
const val STREAM_STATUS_LIVE = "idle"

class StreamRepo @Inject constructor(
    private val synapseService: SynapseService,
    private val gson: Gson,
    private val sharedprefUtil: SharedprefUtil
) {
    private val _streamStatus = MutableStateFlow<String>(STREAM_STATUS_IDLE)
    val streamStatus : StateFlow<String>
        get() = _streamStatus

    private val _startStreamOutput = MutableStateFlow<Resource<StartStreamOutput>>(Resource.Idle())
    val startStreamOutput : StateFlow<Resource<StartStreamOutput>>
        get() = _startStreamOutput

    private val _closeStreamOutput = MutableStateFlow<Resource<CloseStreamOutput>>(Resource.Idle())
    val closeStreamOutput : StateFlow<Resource<CloseStreamOutput>>
        get() = _closeStreamOutput

    private val _allActiveStreams = MutableStateFlow<Resource<AllActiveRoomsOutput>>(Resource.Idle())
    val allActiveStreams : StateFlow<Resource<AllActiveRoomsOutput>>
        get() = _allActiveStreams

    suspend fun startStream(roomName : String){
        _startStreamOutput.emit(Resource.Loading())
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _startStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.startStream(token, StartStreamInput(roomName))
        val resource = handleStartStreamResponse(res)
        _startStreamOutput.emit(resource)

        when(resource){
            is Resource.Success ->{
                _streamStatus.emit(STREAM_STATUS_LIVE)
                sharedprefUtil.putString(SharedprefUtil.ACTIVE_STREAM_KEY, roomName)
            }
            is Resource.Error ->{
                _streamStatus.emit(STREAM_STATUS_IDLE)
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

    suspend fun closeStream(roomName: String){
        _closeStreamOutput.emit(Resource.Loading())
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _closeStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val roomName = sharedprefUtil.getString(SharedprefUtil.ACTIVE_STREAM_KEY)
        if (roomName == null){
            Log.d("TAG", "closeStream: room name is Empty")
            _closeStreamOutput.emit(Resource.Error(message = "Room name didn't found"))
            return
        }

        val res = synapseService.closeStream(token, CloseStreamInput(roomName = roomName))
        val resource = handleCloseStream(res)
        _closeStreamOutput.emit(resource)

        when(resource){
            is Resource.Success ->{
                _streamStatus.emit(STREAM_STATUS_IDLE)
                sharedprefUtil.deleteString(SharedprefUtil.ACTIVE_STREAM_KEY)
            }
            is Resource.Error ->{
                _streamStatus.emit(STREAM_STATUS_LIVE)
            }
            else ->{
                Log.d(TAG, "startStream: Else block...")
            }
        }
    }

    private fun handleCloseStream(res : Response<CloseStreamOutput>) : Resource<CloseStreamOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleCloseStream: error in close stream body is null")
        return Resource.Error(message = "error in start stream body is null")
    }

    suspend fun getAllActiveStreams(){
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _closeStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.getAllActiveStreams(token)
        val resource = handleAllActiveStreams(res)
        _allActiveStreams.emit(resource)

        when(resource){
            is Resource.Success ->{

            }
            is Resource.Error ->{

            }
            else ->{
                Log.d(TAG, "allActiveStreams: Else block...")
            }
        }
    }

    private fun handleAllActiveStreams (res : Response<AllActiveRoomsOutput>) : Resource<AllActiveRoomsOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleAllActiveStreams: error in all active streams body is null")
        return Resource.Error(message = "error in all active streams body is null")
    }
}