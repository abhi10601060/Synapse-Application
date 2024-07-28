package com.example.synapse.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.synapse.R
import com.example.synapse.util.IntentLabel
import com.example.synapse.util.IntentValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartStream : AppCompatActivity() {
    val TAG = "StartStream"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_stream)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.startStreamNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val dest = intent.getStringExtra(IntentLabel.START_STREAM_DESTINATION)
        Log.d(TAG, "onCreate: destination is : $dest")
        dest?.let {
            when(it){
                IntentValue.START_STREAM_LIVE ->{
                    navController.navigate(R.id.action_screenCaptureDetailsFragment_to_livePreviewFragment)

                }
                IntentValue.START_STREAM_SCREEN_CAPTURE ->{

                }
                else -> {

                }
            }
        }
    }
}