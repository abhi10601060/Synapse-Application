package com.example.synapse.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.synapse.R
import io.livekit.android.renderer.SurfaceViewRenderer

class WatchStream : AppCompatActivity() {

    val TAG = "WatchStreamActivity"

    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var chatEdt : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_watch_stream)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        createView()

        intent?.let {
            val streamName = it.getStringExtra("name")
            Log.d(TAG, "onCreate: incoming stream is : $streamName")
        }
    }

    private fun createView() {
        surfaceViewRenderer = findViewById(R.id.watchStreamSurface)
        chatEdt = findViewById(R.id.watchStreamChatEdt)
    }
}