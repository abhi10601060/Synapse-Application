package com.example.synapse.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.synapse.R
import com.example.synapse.model.res.Stream

class ActiveStreamsAdapter(private val streamOnClick : ActiveStreamOnClick) : ListAdapter<Stream, ActiveStreamsAdapter.MyViewHolder>(StreamDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_active_stream, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val stream = getItem(position)

        holder.activeStreamTitle.text = stream.name

        holder.activeStreamThumbnail.setOnClickListener(View.OnClickListener {
            streamOnClick.onStreamClicked(stream)
        })
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView){
        val activeStreamThumbnail : ImageView = itemView.findViewById(R.id.streamThumbnailImg)
        val activeStreamTitle : TextView = itemView.findViewById(R.id.streamTitle)
    }

    class StreamDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Stream>(){
        override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem.name == newItem.name
        }

    }

    interface ActiveStreamOnClick{
        fun onStreamClicked(stream : Stream)
    }
}