package com.example.synapse.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.synapse.R
import com.example.synapse.model.res.Stream

class ActiveStreamsAdapter(private val streamOnClick : ActiveStreamOnClick, private val context: Context?) : ListAdapter<Stream, ActiveStreamsAdapter.MyViewHolder>(StreamDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_active_stream, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val stream = getItem(position)

        holder.activeStreamThumbnail.setOnClickListener(View.OnClickListener {
            streamOnClick.onStreamClicked(stream)
        })

        val profileImageUrl = "https://abhis-s3.s3.ap-south-1.amazonaws.com/${stream.streamerId}/profile-picture"
        holder.activeStreamTitle.text = stream.title
        holder.streamerName.text = stream.streamerId
        holder.streamStartTime.text = stream.createdOn

        if (context != null) {
            Glide.with(context)
                .load(profileImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(holder.streamerProfileImage)

            Glide.with(context)
                .load(stream.thumbNailUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(holder.activeStreamThumbnail)
        }
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView){
        val activeStreamThumbnail : ImageView = itemView.findViewById(R.id.streamThumbnailImg)
        val activeStreamTitle : TextView = itemView.findViewById(R.id.streamTitle)
        val streamerProfileImage : ImageView = itemView.findViewById(R.id.streamerProfileImg)
        val streamerName : TextView = itemView.findViewById(R.id.streamerName)
        val streamStartTime : TextView = itemView.findViewById(R.id.streamStartTime)
    }

    class StreamDiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Stream>(){
        override fun areItemsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Stream, newItem: Stream): Boolean {
            return oldItem.streamId == newItem.streamId
        }

    }

    interface ActiveStreamOnClick{
        fun onStreamClicked(stream : Stream)
    }
}