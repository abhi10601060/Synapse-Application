package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.req.LikeDislikeInput
import com.example.synapse.model.res.ResponseMessageOutput
import com.example.synapse.model.res.StartStreamOutput
import com.example.synapse.model.res.WatchStreamOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.delay
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

    private val _likeDislikeStreamOutput = MutableStateFlow(0)
    val likeDisLikeStreamOutput : StateFlow<Int>
        get() = _likeDislikeStreamOutput

//    private val _removeLikeOfStreamOutput = MutableStateFlow<Resource<ResponseMessageOutput>>(Resource.Idle())
//    val removeLikeOfStreamOutput : StateFlow<Resource<ResponseMessageOutput>>
//        get() = _removeLikeOfStreamOutput
//
//    private val _disLikeStreamOutput = MutableStateFlow<Resource<ResponseMessageOutput>>(Resource.Idle())
//    val disLikeStreamOutput : StateFlow<Resource<ResponseMessageOutput>>
//        get() = _disLikeStreamOutput
//
//    private val _removeDislikeStreamOutput = MutableStateFlow<Resource<ResponseMessageOutput>>(Resource.Idle())
//    val removeDislikeStreamOutput : StateFlow<Resource<ResponseMessageOutput>>
//        get() = _removeDislikeStreamOutput

    suspend fun watchStream(streamName : String){
        _watchStreamOutput.emit(Resource.Loading())
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_VIEWER_TOKEN
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

    suspend fun closeStream(){
        _watchStreamOutput.emit(Resource.Idle())
    }

    suspend fun likeStream(likeStreamInput : LikeDislikeInput){
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_VIEWER_TOKEN
        if (token == null) {
            Log.d("TAG", "startStream: token is Empty")
            _watchStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val res = synapseService.likeStream(token, likeStreamInput)
        val resource = handleLikeDislikeResponse(res)
        if (resource == 1){
            _likeDislikeStreamOutput.emit(11)
        }
        else{
            _likeDislikeStreamOutput.emit(10)
        }
        delay(100)
        _likeDislikeStreamOutput.emit(0)
    }

    suspend fun removeLikeOfStream(removeLikeStreamInput : LikeDislikeInput){
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_VIEWER_TOKEN
        if (token == null) {
            Log.d("TAG", "removeLikeStream: token is Empty")
            _watchStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val res = synapseService.removeLikeOfStream(token, removeLikeStreamInput)
        val resource = handleLikeDislikeResponse(res)
        if (resource == 1){
            _likeDislikeStreamOutput.emit(-11)
        }
        else{
            _likeDislikeStreamOutput.emit(-10)
        }
        delay(100)
        _likeDislikeStreamOutput.emit(0)
    }

    suspend fun dislikeStream(disLikeInput : LikeDislikeInput) {
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_VIEWER_TOKEN
        if (token == null) {
            Log.d("TAG", "removeLikeStream: token is Empty")
            _watchStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val res = synapseService.dislikeStream(token, disLikeInput)
        val resource = handleLikeDislikeResponse(res)
        if (resource == 1){
            _likeDislikeStreamOutput.emit(21)
        }
        else{
            _likeDislikeStreamOutput.emit(20)
        }
        delay(100)
        _likeDislikeStreamOutput.emit(0)
    }

    suspend fun removeDislikeStream(removeDisLikeInput : LikeDislikeInput) {
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
//        token = DUMMY_VIEWER_TOKEN
        if (token == null) {
            Log.d("TAG", "removeLikeStream: token is Empty")
            _watchStreamOutput.emit(Resource.Error(message = "token Invalid"))
            return
        }
        val res = synapseService.removeDislikeOfStream(token, removeDisLikeInput)
        val resource = handleLikeDislikeResponse(res)
        if (resource == 1){
            _likeDislikeStreamOutput.emit(-21)
        }
        else{
            _likeDislikeStreamOutput.emit(-20)
        }
        delay(100)
        _likeDislikeStreamOutput.emit(0)
    }

    private fun handleLikeDislikeResponse(res : Response<ResponseMessageOutput>): Int {
        if (res.isSuccessful && res.body() != null){
            return 1
        }
        Log.d(TAG, "handleLikeDislikeResponse: response in null")
        return 0
    }
}