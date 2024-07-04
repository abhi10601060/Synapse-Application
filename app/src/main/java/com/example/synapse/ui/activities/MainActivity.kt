package com.example.synapse.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.synapse.R
import com.example.synapse.model.DITest
import com.example.synapse.repo.MainRepo
import com.example.synapse.services.ScreenCaptureService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var  diTest : DITest
    @Inject lateinit var mainRepo: MainRepo


    private lateinit var startButton : Button
    private lateinit var stopButton : Button
    private lateinit var surfaceView : SurfaceViewRenderer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createView()


        ScreenCaptureService.surfaceViewRenderer = surfaceView
        askPermission()

        startButton.setOnClickListener(View.OnClickListener {
            startStream()
        })

        stopButton.setOnClickListener(View.OnClickListener {
            stopStream()
        })
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION,
            Manifest.permission.FOREGROUND_SERVICE,) , 200)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200){
            Toast.makeText(this, "Service permission added", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("fafaf")
    private fun startStream() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            ScreenCaptureService.permissionIntent = data
            Log.d("AbhiService", "onActivityResult: ${data.toString()}")
            CoroutineScope(Dispatchers.Unconfined).launch {
                delay(1000)
                startScreenCaptureService()
            }
        }
    }

    private fun startScreenCaptureService() {
        val intent = Intent(applicationContext, ScreenCaptureService::class.java)
        intent.action = "start"
        applicationContext.startForegroundService(intent)
    }

    private fun stopStream() {
        val intent = Intent(applicationContext, ScreenCaptureService::class.java)
        intent.action = "stop"
        applicationContext.startForegroundService(intent)
    }

    private fun createView() {
        startButton = findViewById(R.id.start_stream)
        stopButton = findViewById(R.id.stop_stream)
        surfaceView = findViewById(R.id.surfaceViewRenderer)
    }
}