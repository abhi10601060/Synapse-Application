package com.example.synapse.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.ChatMessage
import com.example.synapse.ui.custom.CustomChatBox
import com.example.synapse.viemodel.WatchStreamViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WatchStream : AppCompatActivity() {

    val TAG = "WatchStreamActivity"

    private val watchStreamViewModel : WatchStreamViewModel by viewModels()

    private lateinit var surfaceViewRenderer: SurfaceViewRenderer
    private lateinit var chatEdt : EditText
    private lateinit var streamName : String
    private lateinit var chatBox: CustomChatBox
    @Inject lateinit var gson : Gson

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
        setLiveChatListener()
        watchStreamViewModel.init(surfaceViewRenderer, LiveKit.create(applicationContext))

        intent?.let {
            streamName = it.getStringExtra("name").toString()
            Log.d(TAG, "onCreate: incoming stream is : $streamName")
        }

        chatEdt.setOnEditorActionListener{_,actionId,_ ->
            if (actionId == EditorInfo.IME_ACTION_SEND){
                Log.d(TAG, "onViewCreated: text is ${chatEdt.text.toString()}: ")
                val text = chatEdt.text.toString()
                if (text.isNotBlank()){
                    watchStreamViewModel.sendChat(text)
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setLiveChatListener() {
        GlobalScope.launch(Dispatchers.Main) {
            watchStreamViewModel.receivedChat.collect{chatMessageJson ->
                if(!chatMessageJson.equals("")){
                    val chatMessage  = gson.fromJson(chatMessageJson, ChatMessage::class.java)
                    Log.d(TAG, "setLiveChatListener: received chat : $chatMessage")

                    chatBox.addChatMessage(chatMessage)
                }
            }
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
        chatBox = findViewById(R.id.watchStreamChatBox)
    }
}