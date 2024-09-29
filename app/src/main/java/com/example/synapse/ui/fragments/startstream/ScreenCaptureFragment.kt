package com.example.synapse.ui.fragments.startstream

import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.ChatMessage
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
class ScreenCaptureFragment : Fragment(R.layout.fragment_screen_capture) {
    private val TAG = "ScreenCaptureFragment"

    private val streamViewModel : StreamViewModel by viewModels()

    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer
    private lateinit var stopStreamBtn : Button
    private lateinit var chatBox : CustomChatBox
    private lateinit var chatEdt : EditText
    @Inject lateinit var gson : Gson

    private var name : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setLiveChatListener()
        setOnClicks()

        streamViewModel.init(streamSurfaceViewRenderer,
            activity?.let { LiveKit.create(it.applicationContext) })

        arguments?.let {
            name = it.getString("name")
            Log.d(TAG, "onViewCreated: received name : $name")
            if (name != null) {
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
        })
    }

    private fun askMediaProjectionPermission() {
        val mediaProjectionManager = activity?.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
        Log.d(TAG, "askMediaProjectionPermission: Permission asked")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            Log.d("LiveKit", "onActivityResult: mediaprojection intent received")
            lifecycleScope.launch {
                name?.let { streamViewModel.startScreenCapture(it, data) }
            }
        }
    }

    private fun createView(view : View) {
        streamSurfaceViewRenderer = view.findViewById(R.id.stream_surface)
        stopStreamBtn = view.findViewById(R.id.screenCaptureStopStreamBtn)
        chatBox = view.findViewById(R.id.screenCaptureChatBox)
        chatEdt = view.findViewById(R.id.screenCaptureChatEdt)
    }
}