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
import com.example.synapse.R
import com.example.synapse.model.data.Subscription

class SubscriptionsAdapter(private val context: Context, private val onProfileClick:(Subscription) -> Unit) : ListAdapter<Subscription, SubscriptionsAdapter.MyViewHolder>(MyDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subscription_profile_layout , parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val subscription = getItem(position)

        if (!subscription.profilePicUrl.isNullOrBlank()){
            Glide.with(context)
                .load(subscription.profilePicUrl)
                .into(holder.profilePic)
        }

        holder.name.text = subscription.id

        if (subscription.status.equals("live")) holder.liveIndicator.visibility = View.VISIBLE

        holder.profilePic.setOnClickListener(View.OnClickListener {
            onProfileClick(subscription)
        })
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView){
        val profilePic = itemView.findViewById<ImageView>(R.id.subscription_profile_Img)
        val name = itemView.findViewById<TextView>(R.id.subscription_name)
        val liveIndicator = itemView.findViewById<View>(R.id.subscription_live_indicator)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<Subscription>(){
        override fun areItemsTheSame(p0: Subscription, p1: Subscription): Boolean {
            return p0 == p1
        }

        override fun areContentsTheSame(p0: Subscription, p1: Subscription): Boolean {
            return p0.id == p1.id
        }

    }
}