package com.example.synapse.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.ChatMessage
import com.example.synapse.model.req.StartStreamInput
import com.example.synapse.ui.custom.CustomChatBox
import com.example.synapse.viemodel.StreamViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCapture : AppCompatActivity() {

    private val TAG = "ScreenCaptureActivity"

    private val streamViewModel : StreamViewModel by viewModels()

    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer
    private lateinit var stopStreamBtn : Button
    private lateinit var chatBox : CustomChatBox
    private lateinit var chatEdt : EditText
    @Inject
    lateinit var gson : Gson

    private var title : String? = null
    private var desc : String = ""
    private var thumbnail : String = ""
    private var tags : String = ""
    private var toSave : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_capture)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createView()
        setLiveChatListener()
        setOnClicks()

        streamViewModel.init(streamSurfaceViewRenderer, LiveKit.create(this.applicationContext))

        intent?.let {
            title = it.getStringExtra("title")
            desc = it.getStringExtra("desc").toString()
            tags = it.getStringExtra("tags").toString()
            thumbnail = it.getStringExtra("thumbnail").toString()
            Log.d(TAG, "onViewCreated: received name : $title")
            if (title != null) {
                askMediaProjectionPermission()
            }
        }

        chatEdt.setOnEditorActionListener{_,actionId,_ ->
            if (actionId == EditorInfo.IME_ACTION_SEND){
                Log.d(TAG, "onViewCreated: text is ${chatEdt.text.toString()}: ")
                val text = chatEdt.text.toString()
                if (text.isNotBlank()){
                    streamViewModel.sendChat(text)
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        Toast.makeText(this, "Close the stream first...", Toast.LENGTH_SHORT).show()
    }
    
    
    @OptIn(DelicateCoroutinesApi::class)
    private fun setLiveChatListener() {
        GlobalScope.launch(Dispatchers.Main) {
            streamViewModel.receivedChat.collect{chatMessageJson ->
                if(!chatMessageJson.equals("")){
                    val chatMessage  = gson.fromJson(chatMessageJson, ChatMessage::class.java)
                    Log.d(TAG, "setLiveChatListener: received chat : $chatMessage")

                    chatBox.addChatMessage(chatMessage)
                }
            }
        }
    }

    private fun setOnClicks() {
        stopStreamBtn.setOnClickListener(View.OnClickListener {
            streamViewModel.stopStream()
            val intent = Intent(this@ScreenCapture, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        })
    }

    private fun askMediaProjectionPermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
        Log.d(TAG, "askMediaProjectionPermission: Permission asked")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            Log.d("LiveKit", "onActivityResult: mediaprojection intent received")
            lifecycleScope.launch {
                title?.let { streamViewModel.startScreenCapture(StartStreamInput(title!!, desc, tags, thumbnail, false), data) }
            }
        }
    }

    private fun createView() {
        streamSurfaceViewRenderer = findViewById(R.id.stream_surface)
        stopStreamBtn = findViewById(R.id.screenCaptureStopStreamBtn)
        chatBox = findViewById(R.id.screenCaptureChatBox)
        chatEdt = findViewById(R.id.screenCaptureChatEdt)
    }

    override fun onDestroy() {
        streamViewModel.stopStream()
        super.onDestroy()
    }

}