package com.example.synapse.application

import android.app.Application
import com.example.synapse.db.SharedprefUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SynapseApp : Application() {

    @Inject lateinit var shredPrefUtil : SharedprefUtil
    override fun onCreate() {
        super.onCreate()

        shredPrefUtil.addAbhiData()
    }
}