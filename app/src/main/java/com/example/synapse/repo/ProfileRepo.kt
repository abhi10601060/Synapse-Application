package com.example.synapse.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

class ProfileRepo @Inject constructor(
    private val synapseService: SynapseService,
    private val sharedprefUtil: SharedprefUtil
) {

    private val _profileDetails = MutableStateFlow<Resource<ProfileDetailsOutPut>>(Resource.Loading())
    val profileDetails : StateFlow<Resource<ProfileDetailsOutPut>>
        get() = _profileDetails

    suspend fun getProfileDetails(){
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
        Log.d(TAG, "getProfileDetails: token is : ${token}")
        Log.d(TAG, "getProfileDetails: token : $token")
        val res = token?.let { synapseService.getOwnProfileDetails(it) }
        _profileDetails.emit(handleProfileDetailResponse(res!!))
    }


    fun handleProfileDetailResponse(res : Response<ProfileDetailsOutPut>) : Resource<ProfileDetailsOutPut>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success<ProfileDetailsOutPut>(data = res.body()!!)
        }
        Log.d(TAG, "handleProfileDetailResponse: error in response")
        return Resource.Error(message = "unable to fetch details")
    }

    fun addAbhiToken(){
        sharedprefUtil.addAbhiData()
    }

    fun addShubhamToken(){
        sharedprefUtil.addShubhamData()
    }
}