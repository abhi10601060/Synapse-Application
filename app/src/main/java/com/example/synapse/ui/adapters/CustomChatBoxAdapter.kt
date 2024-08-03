package com.example.synapse.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.synapse.R
import com.example.synapse.model.ChatMessage

class CustomChatBoxAdapter: ListAdapter<ChatMessage, CustomChatBoxAdapter.MyViewHolder>(MyDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_chat_message, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chatItem = getItem(position)

        holder.userName.text = chatItem.userName
        holder.chatMessage.text = chatItem.message
    }


    class MyViewHolder(itemView: View) : ViewHolder(itemView){
        val userProfileImg : ImageView = itemView.findViewById(R.id.chatUserImg)
        val userName : TextView = itemView.findViewById(R.id.chatUserNameTxt)
        val isStreamerTick : TextView = itemView.findViewById(R.id.chatStreamerTickTxt)
        val chatMessage : TextView = itemView.findViewById(R.id.chatMessageTxt)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<ChatMessage>(){
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.message == newItem.message
        }

    }
}