package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.res.AllActiveStreamOutput
import com.example.synapse.model.res.SearchStreamsOutput
import com.example.synapse.model.res.SearchUserOutput
import com.example.synapse.model.res.Stream
import com.example.synapse.model.res.SubscriptionsOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainRepo @Inject constructor(
    private val synapseService : SynapseService,
    private val sharedprefUtil: SharedprefUtil,
    private val gson: Gson
) {
    private val _activeStreams = MutableStateFlow<Resource<AllActiveStreamOutput>>(Resource.Idle())
    val activeStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = _activeStreams

    private val _sessionTimeOut = MutableStateFlow(false)
    val sessionTimeOut : StateFlow<Boolean>
        get() = _sessionTimeOut

    private val _searchedStreams = MutableStateFlow<Resource<SearchStreamsOutput>>(Resource.Idle())
    val searchedStreams : StateFlow<Resource<SearchStreamsOutput>>
        get() = _searchedStreams

    private val _searchedUsers = MutableStateFlow<Resource<SearchUserOutput>>(Resource.Idle())
    val searchedUsers : StateFlow<Resource<SearchUserOutput>>
        get() = _searchedUsers

    private var token : String? = null

    init {
        token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null){
            GlobalScope.launch {
                _sessionTimeOut.emit(true)
            }
        }
        else{
            GlobalScope.launch {
                _sessionTimeOut.emit(false)
            }
        }
    }

    suspend fun getAllActiveStreams(){
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "FetchAllActive Streams: token is Empty")
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

    suspend fun searchStreams(searchParam : String){
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "FetchAllActive Streams: token is Empty")
            _searchedStreams.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.getSearchedStreams(token, searchParam)
        _searchedStreams.emit(handleSearchStreamsOutput(res))
    }

    private fun handleSearchStreamsOutput(res : Response<SearchStreamsOutput>): Resource<SearchStreamsOutput> {
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleAllActiveStreams: error in all active streams body is null")
        return Resource.Error(message = "error in all active streams body is null")
    }

    suspend fun searchPeoples(searchParam : String){
        var token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        if (token == null) {
            Log.d("TAG", "FetchAllActive Streams: token is Empty")
            _searchedStreams.emit(Resource.Error(message = "token Invalid"))
            return
        }

        val res = synapseService.getSearchedUsers(token, searchParam)
        _searchedUsers.emit(handleSearchedPeoplesOutput(res))
    }

    private fun handleSearchedPeoplesOutput(res : Response<SearchUserOutput>): Resource<SearchUserOutput> {
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleAllActiveStreams: error in all active streams body is null")
        return Resource.Error(message = "error in all active streams body is null")
    }


    //******************************************************* Subscription Page **************************************************

    private val _subscriptions = MutableStateFlow<Resource<SubscriptionsOutput>>(Resource.Idle())
    val subscriptions : StateFlow<Resource<SubscriptionsOutput>>
        get() = _subscriptions

    private val _subscriptionStreams = MutableStateFlow<Resource<AllActiveStreamOutput>>(Resource.Idle())
    val subscriptionStreams : StateFlow<Resource<AllActiveStreamOutput>>
        get() = _subscriptionStreams

    suspend fun getAllSubscriptions(){
        val res = synapseService.getAllSubscriptions(token!!)
        _subscriptions.emit(handleSubscriptionsOutput(res))
    }

    fun handleSubscriptionsOutput(res : Response<SubscriptionsOutput>) : Resource<SubscriptionsOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data =  res.body()!!)
        }
        Log.d(TAG, "handleSubscriptionsOutput: error in getting all subscriptions body is null")
        return Resource.Error()
    }

    suspend fun getAllSubscriptionStreams() {
        val res = synapseService.getAllActiveStreams(token!!)
        _subscriptionStreams.emit(handleAllActiveStreams(res))
    }
}