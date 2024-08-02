package com.example.synapse.ui.fragments.startstream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.ChatMessage
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
class LiveFragment : Fragment(R.layout.fragment_live) {
    private val TAG = "LiveFragment"

    private val streamViewModel : StreamViewModel by viewModels()

    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer
    private lateinit var stopStreamBtn : Button
    private lateinit var chatEdt : EditText
    private lateinit var chatText : TextView
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
                if (isPermissionsGranted()){
                    streamViewModel.startLive(name!!)
                }
                else{
                    askPermissions()
                }
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

                    val totalChat = chatText.text.toString() + "\n" + chatMessage.message
                    chatText.text = totalChat
                }
            }
        }
    }

    private fun askPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) , 200)
    }

    private fun isPermissionsGranted() : Boolean{
        context?.let {
            val cameraPermission = ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val micPermission = ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            return cameraPermission && micPermission
        }
        Log.d(TAG, "isPermissionsGranted: context is null")
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(activity, "Permission granted...", Toast.LENGTH_SHORT).show()
        }
        else{
            askPermissions()
        }
    }

    private fun setOnClicks() {
        stopStreamBtn.setOnClickListener(View.OnClickListener {
            streamViewModel.stopStream()
        })
    }

    private fun createView(view: View) {
        stopStreamBtn = view.findViewById(R.id.liveStopStreamBtn)
        streamSurfaceViewRenderer = view.findViewById(R.id.liveStreamsurface)
        chatEdt = view.findViewById(R.id.liveChatEdt)
        chatText = view.findViewById(R.id.liveChatText)
    }
}