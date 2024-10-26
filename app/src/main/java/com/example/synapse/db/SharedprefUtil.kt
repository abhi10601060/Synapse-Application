package com.example.synapse.db

import android.content.Context
import android.content.SharedPreferences

class SharedprefUtil() {
    private lateinit var sharedPreferences: SharedPreferences
    companion object{
        const val SHAREDPREF_ID = "synapse_pref"
        const val USER_TOKEN_KEY = "user_token"
        const val ACTIVE_STREAM_KEY = "active_stream"
        const val USER_ID = "user_id"
        const val PROFILE_PIC_URL = "profile_pic_url"
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

    fun addAbhiData(){
        putString(USER_TOKEN_KEY, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6IkFiaGkiLCJleHAiOjE3MzExMzIxNjR9.4hBaVl4ZpL8eK-aOGNsq1Yoepka3t26MCGG4vo9yNzo")
    }

    fun addShubhamData(){
        putString(USER_TOKEN_KEY, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJJZCI6IlNodWJoYW0iLCJleHAiOjE3MzExMzIyNTB9.xldj_N8qx5VF0e_xyv4xL5Bj6ABGGz1oEHeci3y2Iuk")
    }
}