package com.example.synapse.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.synapse.R
import com.example.synapse.model.data.ProfileDetails
import com.example.synapse.model.data.Subscription

class SearchedProfileAdapter(private val context : Context) : ListAdapter<ProfileDetails, SearchedProfileAdapter.MyViewHolder>(MyDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_home_searched_profile , parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val profile = getItem(position)

        Glide.with(context)
            .load(profile.profilePictureUrl)
            .into(holder.profileImg)

        holder.profileId.text = profile.userName
        holder.subscriberCount.text = profile.totalSubs + " K"
    }


    class MyViewHolder(itemView: View) : ViewHolder(itemView){
        val profileImg : ImageView = itemView.findViewById(R.id.searched_profile_img)
        val profileId : TextView = itemView.findViewById(R.id.searched_profile_id)
        val subscriberCount : TextView = itemView.findViewById(R.id.searched_profile_subs)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<ProfileDetails>(){
        override fun areItemsTheSame(oldItem: ProfileDetails, newItem: ProfileDetails): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ProfileDetails, newItem: ProfileDetails): Boolean {
            return oldItem.userName == newItem.userName
        }

    }

}