package com.example.synapse.network.http


sealed class Resource <T>(
    val message : String? = null,
    val data : T? = null
) {
    class Loading <T>: Resource<T>()
    class Error <T> (message: String , data: T? = null) : Resource<T>(message = message, data= data)
    class Success <T>(data: T) : Resource<T> (data = data)

}