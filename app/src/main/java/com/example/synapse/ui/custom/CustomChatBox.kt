package com.example.synapse.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synapse.R
import com.example.synapse.model.ChatMessage
import com.example.synapse.ui.adapters.CustomChatBoxAdapter

class CustomChatBox (
    context : Context,
    attrs : AttributeSet? = null
):  FrameLayout(context, attrs){

    private lateinit var chatBoxRv : RecyclerView
    private var chatList = ArrayList<ChatMessage>()
    private val adapter = CustomChatBoxAdapter()

    init {
        val view = inflate(context, R.layout.layout_custom_chat_box, this)
        chatBoxRv = view.findViewById(R.id.customChatBoxRV)
        adapter.submitList(chatList)
        chatBoxRv.adapter = adapter
        chatBoxRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        attrs?.let {

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addChatMessage(chatMessage: ChatMessage){
        chatList.add(chatMessage)
        adapter.notifyDataSetChanged()
    }
}