package com.example.synapse.repo

import android.util.Log
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.req.AuthenticationInput
import com.example.synapse.model.res.AuthenticationOutput
import com.example.synapse.network.AuthService
import com.example.synapse.network.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import javax.inject.Inject

class AuthRepo @Inject constructor(
    private val authService: AuthService,
    private val sharedprefUtil: SharedprefUtil
) {

    private val _loginResult = MutableStateFlow<Resource<AuthenticationOutput>>(Resource.Idle())
    val loginResult : StateFlow<Resource<AuthenticationOutput>>
        get() = _loginResult

    private val _signupResult = MutableStateFlow<Resource<AuthenticationOutput>>(Resource.Idle())
    val signupResult : StateFlow<Resource<AuthenticationOutput>>
        get() = _signupResult

    suspend fun login(authInput : AuthenticationInput){
        val res = authService.login(authInput)
        _loginResult.emit(handleAuthOutput(res))
    }

    suspend fun signup(authInput : AuthenticationInput){
        val res = authService.signup(authInput)
        _signupResult.emit(handleAuthOutput(res))
    }

    private fun handleAuthOutput(res : Response<AuthenticationOutput>) : Resource<AuthenticationOutput>{
        Log.d(TAG, "handleAuthOutput: code : = ${res.code()}")
        if (res.isSuccessful && res.body() != null && res.code() == 200){
            return Resource.Success(data =  res.body()!!)
        }
        return Resource.Error()
    }

    suspend fun saveUserToken(token : String){
        sharedprefUtil.putString(SharedprefUtil.USER_TOKEN_KEY, token)
    }

    suspend fun removeUserToken(){
        sharedprefUtil.deleteString(SharedprefUtil.USER_TOKEN_KEY)
    }

     fun getUserToken() : String?{
        return sharedprefUtil.getString(SharedprefUtil.USER_TOKEN_KEY)
    }
}