package com.example.synapse.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.synapse.R
import com.example.synapse.db.SharedprefUtil
import com.example.synapse.model.ChatMessage
import com.example.synapse.model.req.LikeDislikeInput
import com.example.synapse.model.req.SubscribeUnsubscribeInput
import com.example.synapse.model.res.Stream
import com.example.synapse.network.Resource
import com.example.synapse.ui.custom.CustomChatBox
import com.example.synapse.util.slideDown
import com.example.synapse.viemodel.STREAM_STATUS_IDLE
import com.example.synapse.viemodel.STREAM_STATUS_LIVE_ERROR
import com.example.synapse.viemodel.STREAM_STATUS_LOADED
import com.example.synapse.viemodel.STREAM_STATUS_LOADING
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
    private lateinit var incomingStreamJson : String
    private lateinit var chatBox: CustomChatBox
    private lateinit var openLiveChat : RelativeLayout
    private lateinit var chatBoxParentRL : RelativeLayout
    private lateinit var closeLiveChat : ImageView
    private lateinit var streamTitle : TextView
    private lateinit var streamStartTime : TextView
    private lateinit var streamerName : TextView
    private lateinit var streamerProfilePic : ImageView
    private lateinit var likesCount : TextView
    private lateinit var disLikesCount : TextView
    private lateinit var likeImg : ImageView
    private lateinit var disLikeImg : ImageView
    private lateinit var loadingStreamRL : RelativeLayout
    private lateinit var loadinStreamThumbnail : ImageView
    private lateinit var subscribeBtn : Button

    private lateinit var incomingStream : Stream

    private var isLiked = false
    private var isDisliked = false

    @Inject lateinit var gson : Gson
    @Inject lateinit var sharedprefUtil: SharedprefUtil

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
        setStreamActionListeners()
        setStreamStatusListner()
        setLiveChatListener()
        watchStreamViewModel.init(surfaceViewRenderer, LiveKit.create(applicationContext))

        intent?.let {
            incomingStreamJson = it.getStringExtra("stream").toString()
            Log.d(TAG, "onCreate: incoming stream json is : $incomingStreamJson")
            incomingStream = gson.fromJson(incomingStreamJson, Stream::class.java)
            Log.d(TAG, "onCreate: incoming stream is : $incomingStream")
            setStreamData(incomingStream)
        }

        chatEdt.setOnEditorActionListener{_,actionId,_ ->
            if (actionId == EditorInfo.IME_ACTION_SEND){
                Log.d(TAG, "onViewCreated: text is ${chatEdt.text.toString()}: ")
                val text = chatEdt.text.toString()
                if (text.isNotBlank()){
                    watchStreamViewModel.sendChat(text)
                    addChatMessage(text)
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        chatBox.addChatMessage(ChatMessage(userName, message = text, profilePicUrl, false))
    }

    private fun setStreamStatusListner() {
        lifecycleScope.launch(Dispatchers.Main) {
            watchStreamViewModel.streamStatus.collect{
                when(it){
                    STREAM_STATUS_LOADED ->{
                        loadingStreamRL.visibility = View.GONE
                        surfaceViewRenderer.visibility = View.VISIBLE
                    }

                    STREAM_STATUS_LOADING ->{
                        loadingStreamRL.visibility = View.VISIBLE
                        surfaceViewRenderer.visibility = View.INVISIBLE
                    }

                    STREAM_STATUS_LIVE_ERROR ->{
                        Toast.makeText(this@WatchStream, "Error while connecting stream!!!", Toast.LENGTH_SHORT).show()
                    }

                    STREAM_STATUS_IDLE ->{
                        Log.d(TAG, "setStreamStatusListner: Stream status idle" )
                    }

                }
            }
        }
    }

    private fun setStreamActionListeners() {
        lifecycleScope.launch(Dispatchers.Main) {
            watchStreamViewModel.likeStreamOutput.collect{
                when(it){
                    11 ->{
                        likeImg.setImageResource(R.drawable.solid_thumb_up)
                        likesCount.text = (incomingStream.likes + 1).toString()
                    }
                    10 ->{
                        Toast.makeText(baseContext, "error in like", Toast.LENGTH_SHORT).show()
                        likeImg.setImageResource(R.drawable.outline_thumb)
                    }
                    -11 ->{
                        likeImg.setImageResource(R.drawable.outline_thumb)
                        likesCount.text = incomingStream.likes.toString()
                    }
                    -10 ->{
                        Toast.makeText(baseContext, "error in remove like", Toast.LENGTH_SHORT).show()
                        likeImg.setImageResource(R.drawable.solid_thumb_up)
                    }
                    21 ->{
                        disLikeImg.setImageResource(R.drawable.solid_thumb_down)
                        disLikesCount.text = (incomingStream.dislikes +1).toString()
                    }
                    20 ->{
                        Log.d(TAG, "setStreamActionListeners: error in dislike")
                        disLikeImg.setImageResource(R.drawable.outline_thumb_down)
                    }
                    -21 ->{
                        disLikeImg.setImageResource(R.drawable.outline_thumb_down)
                        disLikesCount.text = incomingStream.dislikes.toString()
                    }
                    -20 ->{
                        Log.d(TAG, "setStreamActionListeners: error in remove dislike")
                        disLikeImg.setImageResource(R.drawable.solid_thumb_down)
                    }
                    else ->{

                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            watchStreamViewModel.subUnSubStatus.collect{status ->
                when(status){
                    10 ->{
                        Toast.makeText(this@WatchStream, "error in subscribing streamer", Toast.LENGTH_SHORT).show()
                    }
                    11 ->{
                        subscribeBtn.text = "Unsubscribe"
                    }
                    -10 ->{
                        Toast.makeText(this@WatchStream, "error in un-subscribing streamer", Toast.LENGTH_SHORT).show()
                    }
                    -11 ->{
                        subscribeBtn.text = "Subscribe"
                    }
                    else ->{

                    }
                }
            }
        }
    }

    private fun setStreamData(stream: Stream) {
        streamTitle.text = stream.title
        likesCount.text = stream.likes.toString()
        disLikesCount.text = stream.dislikes.toString()
        streamerName.text = stream.streamerId
        streamStartTime.text = stream.createdOn
        Glide.with(this)
            .load(stream.thumbNailUrl)
            .into(loadinStreamThumbnail)
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
        watchStreamViewModel.watchStream(incomingStream.streamId)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called")
        watchStreamViewModel.closeStream()
    }

    private fun setOnClicks() {
        openLiveChat.setOnClickListener(View.OnClickListener {
            openLiveChat.visibility = View.GONE
            chatBoxParentRL.visibility = View.VISIBLE
            chatBoxParentRL.slideDown(300, 0)
            closeLiveChat.visibility = View.VISIBLE
        })

        closeLiveChat.setOnClickListener(View.OnClickListener {
            chatBoxParentRL.visibility = View.GONE
            openLiveChat.visibility = View.VISIBLE
            closeLiveChat.visibility = View.GONE
        })

        likeImg.setOnClickListener(View.OnClickListener {
            val likeDislikeInput = LikeDislikeInput(incomingStream.streamId)
            if (isDisliked){
                watchStreamViewModel.removeDislikeOfStream(likeDislikeInput)
            }
            if (isLiked){
                isLiked = !isLiked
                watchStreamViewModel.removeLikeOfStream(likeDislikeInput)
            }else{
                isLiked = !isLiked
                watchStreamViewModel.likeStream(likeDislikeInput)
            }
        })

        disLikeImg.setOnClickListener(View.OnClickListener {
            val likeDislikeInput = LikeDislikeInput(incomingStream.streamId)
            if (isLiked){
                watchStreamViewModel.removeLikeOfStream(likeDislikeInput)
            }
            if (isDisliked){
                isDisliked = !isDisliked
                watchStreamViewModel.removeDislikeOfStream(likeDislikeInput)
            }
            else{
                isDisliked = !isDisliked
                watchStreamViewModel.dislikeStream(likeDislikeInput)
            }
        })

        subscribeBtn.setOnClickListener(View.OnClickListener {
            val status = watchStreamViewModel.subUnSubStatus.value
            if (status == -10 || status == 11){
                watchStreamViewModel.unsubscribeStreamer(SubscribeUnsubscribeInput(incomingStream.streamerId))
            }else{
                watchStreamViewModel.subscribeStreamer((SubscribeUnsubscribeInput(incomingStream.streamerId)))
            }
        })
    }

    private fun createView() {
        surfaceViewRenderer = findViewById(R.id.watchStreamSurface)
        chatEdt = findViewById(R.id.watchStreamChatEdt)
        chatBox = findViewById(R.id.watchStreamChatBox)
        openLiveChat = findViewById(R.id.watchStreamOpenLiveChatRL)
        chatBoxParentRL = findViewById(R.id.watchStreamChatBoxParentRL)
        closeLiveChat = findViewById(R.id.closeLiveChatImg)
        streamTitle = findViewById(R.id.watchStreamTitleTxt)
        streamStartTime = findViewById(R.id.watchStreamTime)
        streamerName = findViewById(R.id.watchStreamStreamerName)
        likesCount = findViewById(R.id.watchStreamLikeTxt)
        disLikesCount = findViewById(R.id.watchStreamDislikeTxt)
        likeImg = findViewById(R.id.watchStreamLikeImg)
        disLikeImg = findViewById(R.id.watchStreamDisLikeImg)
        streamerProfilePic = findViewById(R.id.watchStreamStreamerProfilePicImg)
        loadingStreamRL = findViewById(R.id.watchStreamLoadingThumbnailRL)
        loadinStreamThumbnail = findViewById(R.id.watchStreamLoadingThumbnailImg)
        subscribeBtn = findViewById(R.id.watchStreamSubscribeBtn)
    }
}