package com.example.synapse.network

sealed class Resource <T> (
    val data : T? = null,
    val message : String? = null
){
    class Idle<T>() : Resource<T>()
    class Loading<T>(message: String? = null): Resource<T> (message = message)
    class Success<T>(data: T , message: String? = null) : Resource<T>(data = data, message = message)
    class Error<T>(data: T? = null, message: String?=null) : Resource<T>(data = data, message= message)
}