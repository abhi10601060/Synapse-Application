package com.example.synapse.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.viemodel.WatchStreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchStream : AppCompatActivity() {

    val TAG = "WatchStreamActivity"

    private val watchStreamViewModel : WatchStreamViewModel by viewModels()

    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var watchStreamBtn : Button
    private lateinit var chatEdt : EditText
    private lateinit var streamName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_watch_stream)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        createView()
        setOnClicks()
        watchStreamViewModel.init(surfaceViewRenderer, LiveKit.create(applicationContext))

        intent?.let {
            streamName = it.getStringExtra("name").toString()
            Log.d(TAG, "onCreate: incoming stream is : $streamName")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
        watchStreamViewModel.watchStream(streamName)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called")
        watchStreamViewModel.closeStream()
    }

    private fun setOnClicks() {

    }

    private fun createView() {
        surfaceViewRenderer = findViewById(R.id.watchStreamSurface)
        chatEdt = findViewById(R.id.watchStreamChatEdt)
        watchStreamBtn = findViewById(R.id.watchStreamBtn)
    }
}