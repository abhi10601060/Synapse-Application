package com.example.synapse.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.synapse.R
import com.example.synapse.model.data.Subscription
import com.example.synapse.util.IntentValue
import com.google.gson.Gson

class ProfileDetailsFragment : Fragment(R.layout.fragment_profile_details) {

    private lateinit var profileImage : ImageView
    private lateinit var name : TextView
    private lateinit var bio : TextView
    private lateinit var subCount : TextView
    private lateinit var subscriptionsCount : TextView

    private lateinit var incomingProfile : Subscription

    private val gson = Gson()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        val bundle = arguments
        bundle?.let {
            val incomingProfileString = bundle.getString(IntentValue.INCOMING_PROFILE)
            incomingProfile = gson.fromJson(incomingProfileString, Subscription::class.java)

            Glide.with(this)
                .load(incomingProfile.profilePicUrl)
                .into(profileImage)

            name.text = incomingProfile.id
            bio.text = incomingProfile.bio
            subCount.text = incomingProfile.totalSubs
            subscriptionsCount.text = incomingProfile.totalStreams
        }
    }

    private fun createView(view: View) {
        profileImage = view.findViewById(R.id.profileDetailsPictureImg)
        name = view.findViewById(R.id.profileDetailsName)
        bio = view.findViewById(R.id.profileDetailsBioTxt)
        subCount = view.findViewById(R.id.profileDetailsSubscriberTxt)
        subscriptionsCount = view.findViewById(R.id.profileDetailsSubscriptionsTxt)
    }
}