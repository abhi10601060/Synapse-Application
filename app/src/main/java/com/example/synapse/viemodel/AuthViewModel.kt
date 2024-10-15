package com.example.synapse.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.synapse.model.req.AuthenticationInput
import com.example.synapse.model.res.AuthenticationOutput
import com.example.synapse.network.Resource
import com.example.synapse.repo.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepo
) : ViewModel() {

    val loginResult : StateFlow<Resource<AuthenticationOutput>>
        get() = authRepo.loginResult

    val signupResult : StateFlow<Resource<AuthenticationOutput>>
        get() = authRepo.signupResult

    fun login(authInput : AuthenticationInput){
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.login(authInput)
        }
    }

    fun signup(authInput : AuthenticationInput){
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.signup(authInput)
        }
    }

    fun saveUserToken(token : String){
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.saveUserToken(token)
        }
    }

    fun removeUserToken(){
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.removeUserToken()
        }
    }

    fun getUserToken() : String? {
        return authRepo.getUserToken()
    }

}