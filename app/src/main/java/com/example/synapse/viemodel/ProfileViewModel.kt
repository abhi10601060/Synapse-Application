package com.example.synapse.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.model.res.UpdateProfileOutput
import com.example.synapse.network.Resource
import com.example.synapse.repo.ProfileRepo
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepo
) : ViewModel() {

    private val _editImageState = MutableStateFlow(false)
    val editImageState : StateFlow<Boolean>
        get() = _editImageState

    private val _editBioState = MutableStateFlow(false)
    val editBioState : StateFlow<Boolean>
        get() = _editBioState

    val profileDetails : StateFlow<Resource<ProfileDetailsOutPut>>
        get() = profileRepo.profileDetails

    val updateProfilePicOutPut : StateFlow<Resource<UpdateProfileOutput>>
        get() = profileRepo.updateProfilePicOutput
    val updateBioOutPut : StateFlow<Resource<UpdateProfileOutput>>
        get() = profileRepo.updateBioOutput

    fun getOwnProfileDetails(){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.getProfileDetails()
        }
    }

    fun onEditImageState(){
        viewModelScope.launch {
            _editImageState.emit(true)
        }
    }

    fun closeEditImageState(){
        viewModelScope.launch {
            _editImageState.emit(false)
        }
    }

    fun onEditBioState(){
        viewModelScope.launch {
            _editBioState.emit(true)
        }
    }

    fun closeEditBioState(){
        viewModelScope.launch {
            _editBioState.emit(false)
        }
    }

    fun addAbhiData(){
        profileRepo.addAbhiToken()
    }

    fun addShubhamData(){
        profileRepo.addShubhamToken()
    }

    fun updateProfilePic(img : String){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.updateProfilePic(img)
        }
    }

    fun updateBio(bio : String){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.updateBio(bio)
        }
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO) {
            profileRepo.logout()
        }
    }
}