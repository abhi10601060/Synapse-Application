package com.example.synapse.db

import android.content.Context
import android.content.SharedPreferences

class SharedprefUtil() {
    private lateinit var sharedPreferences: SharedPreferences
    companion object{
        const val SHAREDPREF_ID = "synapse_pref"
        const val USER_TOKEN_KEY = "user_token"
        const val ACTIVE_STREAM_KEY = "active_stream"
    }


    constructor(context: Context) : this(){
        sharedPreferences = context.getSharedPreferences(SHAREDPREF_ID, 0)
    }

    fun putString(key : String, value : String){
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key : String) : String?{
        return sharedPreferences.getString(key, null)
    }

    fun deleteString(key : String){
        sharedPreferences.edit().remove(key).apply()
    }
}