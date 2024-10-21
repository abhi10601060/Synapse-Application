package com.example.synapse.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.req.UpdateBioInput
import com.example.synapse.model.req.UpdateProfilePicInput
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.model.res.UpdateProfileOutput
import com.example.synapse.network.Resource
import com.example.synapse.network.SynapseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Response
import javax.inject.Inject

class ProfileRepo @Inject constructor(
    private val synapseService: SynapseService,
    private val sharedprefUtil: SharedprefUtil
) {

    private val _profileDetails = MutableStateFlow<Resource<ProfileDetailsOutPut>>(Resource.Loading())
    val profileDetails : StateFlow<Resource<ProfileDetailsOutPut>>
        get() = _profileDetails

    private val _updateProfilePicOutput = MutableStateFlow<Resource<UpdateProfileOutput>>(Resource.Idle())
    val updateProfilePicOutput : StateFlow<Resource<UpdateProfileOutput>>
        get() = _updateProfilePicOutput

    private val _updateBioOutput = MutableStateFlow<Resource<UpdateProfileOutput>>(Resource.Idle())
    val updateBioOutput : StateFlow<Resource<UpdateProfileOutput>>
        get() = _updateBioOutput

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

    suspend fun updateProfilePic(img : String){
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)

        val res = token?.let { synapseService.updateProfilePic(it, UpdateProfilePicInput(img)) }
        _updateProfilePicOutput.emit(handleProfileUpdate(res!!))
        delay(500)
        _updateProfilePicOutput.emit(Resource.Idle())
    }
    suspend fun updateBio(bio :String){
        val token = sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)

        val res = token?.let { synapseService.updateBio(it, UpdateBioInput(bio)) }
        _updateBioOutput.emit(handleProfileUpdate(res!!))
        delay(500)
        _updateBioOutput.emit(Resource.Idle())
    }

    private fun handleProfileUpdate(res : Response<UpdateProfileOutput>) : Resource<UpdateProfileOutput>{
        if (res.isSuccessful && res.body() != null){
            return Resource.Success(data = res.body()!!)
        }
        return Resource.Error(message = "error in profile pic update")
    }

    fun addAbhiToken(){
        sharedprefUtil.addAbhiData()
    }

    fun addShubhamToken(){
        sharedprefUtil.addShubhamData()
    }

    fun logout(){
        sharedprefUtil.deleteString(SharedprefUtil.USER_TOKEN_KEY)
    }
}