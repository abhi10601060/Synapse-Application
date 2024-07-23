package com.example.synapse.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.DITest
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.launch
import livekit.org.webrtc.RendererCommon
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val VIEWER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4MTExNDM0MTMsImlzcyI6IkFQSUZRTFA1UUFmOFF3aCIsIm5iZiI6MTcyMTE0MzQxMywic3ViIjoidmlld2VyIiwidmlkZW8iOnsiY2FuUHVibGlzaCI6ZmFsc2UsImNhblB1Ymxpc2hEYXRhIjp0cnVlLCJjYW5TdWJzY3JpYmUiOnRydWUsInJvb20iOiJzeW5hcHNlLXRlc3QiLCJyb29tSm9pbiI6dHJ1ZX19.urnd-5rnxw_EvGm70thWMjR4eUj8zJHM8sdUGwgr3fc"
    private val STREAMER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MzAxNDMzMDgsImlzcyI6IkFQSUZRTFA1UUFmOFF3aCIsIm5iZiI6MTcyMTE0MzMwOCwic3ViIjoic3RyZWFtZXIiLCJ2aWRlbyI6eyJjYW5QdWJsaXNoIjp0cnVlLCJjYW5QdWJsaXNoRGF0YSI6dHJ1ZSwiY2FuU3Vic2NyaWJlIjp0cnVlLCJyb29tIjoic3luYXBzZS10ZXN0Iiwicm9vbUpvaW4iOnRydWV9fQ.v-TEcMGlLwZjNJD0Y1DJW7K3OZ6AVFmiC8IXxkl4E98"
    private val ROOM_NAME = "synapse-test"
    private val WS_URL = "wss://synapse-wj7x7eni.livekit.cloud"
    private val API_KEY = "APIFQLP5QAf8QwhAPIFQLP5QAf8Qwh"
    private val SECRET_KEY = "SZOeMU0GetYpTFWXkVxGNLwSqGDHsJNf35S0GDeJF8uB"

    @Inject lateinit var  diTest : DITest

    private lateinit var joinBtn : Button
    private lateinit var startBtn : Button
    private lateinit var stopBtn : Button
    private lateinit var closeBtn : Button
    private lateinit var sendBtn : Button
    private lateinit var chatBox : EditText
    private lateinit var castBtn : Button
    private lateinit var receivedMessage : TextView

    //liveKit related
    private lateinit var room : Room
    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var streamer : LocalParticipant

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

        room = LiveKit.create(applicationContext)
        room.initVideoRenderer(surfaceViewRenderer)

        if(!checkPermission()){
            askPermissions()
        }

        startBtn.setOnClickListener(View.OnClickListener {
            lifecycleScope.launch {
                startStream(false)
            }
        })

        closeBtn.setOnClickListener(View.OnClickListener {
            closeStream()
        })

        joinBtn.setOnClickListener(View.OnClickListener {
            lifecycleScope.launch {
                watchStream()
            }
        })

        castBtn.setOnClickListener(View.OnClickListener {
            askMediaProjectionPermission()
        })

        sendBtn.setOnClickListener(View.OnClickListener {
            val text = chatBox.text.toString()
            if (!text.isNullOrBlank()){
                lifecycleScope.launch {
                    streamer.publishData(text.toByteArray())
                }
            }
        })
    }

    private suspend fun startStream(isMediaProjection : Boolean, mediaPermissionIntent: Intent? = null){
        lifecycleScope.launch {
            room.events.collect {event ->
                when(event){
                    is RoomEvent.TrackSubscribed ->{
                        val track = event.track
                        if (track is VideoTrack){
                            Log.d("LiveKit", "onTrack : track is videoTrack")
                            val height = event.publication.dimensions?.height
                            val width = event.publication.dimensions?.width

                            Log.d("Livekit", "Remote stream startStream: height : $height and width : $width")
                            attachVideo(track)
                        }
                        else{
                            Log.d("LiveKit", "onTrack : track is not videoTrack")
                        }
                    }
                    is RoomEvent.DataReceived ->{
                        var text = receivedMessage.text
                        var message =  String(event.data)
                        Log.d("LiveKit", "startStream: data: ${event.data} and string: message")
                        receivedMessage.text = text.toString() + message
                    }
                    else -> {
                        Log.d("LiveKit", "startStream: else block")
                    }


                }
            }
        }

        room.connect(WS_URL, STREAMER_TOKEN)
        streamer = room.localParticipant

        if (!isMediaProjection){
            streamer.setCameraEnabled(true)
            streamer.setMicrophoneEnabled(true)
        }
        else {
            streamer.setScreenShareEnabled(true, mediaPermissionIntent)
        }

        while (true){
            if (streamer.videoTrackPublications.size>0){
                val track = streamer.videoTrackPublications.get(0).second as VideoTrack
                val height = streamer.videoTrackPublications.get(0).first.dimensions?.height
                val width = streamer.videoTrackPublications.get(0).first.dimensions?.width

                Log.d("Livekit", "startStream: height : $height and width : $width")

                attachVideo(track)
                break
            }
        }
    }

    private fun askMediaProjectionPermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            Log.d("LiveKit", "onActivityResult: mediaprojection intent received")
            lifecycleScope.launch {
                startStream(true, data)
            }
        }
    }

    private suspend fun watchStream(){
        lifecycleScope.launch {
            room.connect(WS_URL, VIEWER_TOKEN)

            val remoteVideoTrack = room.remoteParticipants.values.firstOrNull()
                ?.getTrackPublication(Track.Source.CAMERA)
                ?.track as? VideoTrack

            if (remoteVideoTrack != null){
                attachVideo(remoteVideoTrack)
            }else{
                Log.d("LiveKit", "watchStream: remote stream is null")
            }
        }
    }

    private fun closeStream(){
        room.disconnect()
    }

    private fun attachVideo(videoTrack : VideoTrack){
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        videoTrack.addRenderer(surfaceViewRenderer)
    }
    private fun checkPermission() : Boolean{
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val microphonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        return cameraPermission && microphonePermission
    }

    private fun askPermissions(){
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) , 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission granted...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createView() {
        sendBtn = findViewById(R.id.send_btn)
        closeBtn = findViewById(R.id.close_btn)
        stopBtn = findViewById(R.id.stop_btn)
        startBtn = findViewById(R.id.start_btn)
        joinBtn = findViewById(R.id.join_btn)
        chatBox = findViewById(R.id.chat_box)
        castBtn = findViewById(R.id.cast_btn)
        receivedMessage = findViewById(R.id.received_msg)

        surfaceViewRenderer = findViewById(R.id.stream_surface)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Livekit", "onDestroy: called")
        room.disconnect()
    }
}