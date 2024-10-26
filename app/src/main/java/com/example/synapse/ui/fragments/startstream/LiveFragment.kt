package com.example.synapse.ui.fragments.startstream

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.db.SharedprefUtil
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
class LiveFragment : Fragment(R.layout.fragment_live) {
    private val TAG = "LiveFragment"

    private val streamViewModel : StreamViewModel by viewModels()

    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer
    private lateinit var stopStreamBtn : Button
    private lateinit var chatEdt : EditText
    private lateinit var chatBox : CustomChatBox
    @Inject lateinit var gson : Gson

    private var title : String? = null
    private var desc : String = ""
    private var thumbnail : String = ""
    private var tags : String = ""
    private var toSave : String? = null

    @Inject lateinit var sharedprefUtil: SharedprefUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setLiveChatListener()
        setOnClicks()

        streamViewModel.init(streamSurfaceViewRenderer,
            activity?.let { LiveKit.create(it.applicationContext) })

        arguments?.let {
            title = it.getString("title")
            desc = it.getString("desc").toString()
            tags = it.getString("tags").toString()
            thumbnail = it.getString("thumbnail").toString()

            Log.d(TAG, "onViewCreated: received name : $title, desc: $desc, tags: $tags")
            if (title != null) {
                if (isPermissionsGranted()){
                    streamViewModel.startLive(StartStreamInput(title!!, desc, tags, thumbnail, false))
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
                    addChatMessage(text)
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(chatEdt.getWindowToken(), 0);
                    chatEdt.setText("")
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun addChatMessage(text: String) {
        val userName = sharedprefUtil.getString(SharedprefUtil.USER_ID)!!
        val profilePicUrl = sharedprefUtil.getString(SharedprefUtil.PROFILE_PIC_URL)!!
        chatBox.addChatMessage(ChatMessage(userName, message = text, profilePicUrl, true))
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
        chatBox = view.findViewById(R.id.liveChatBox)
    }
}