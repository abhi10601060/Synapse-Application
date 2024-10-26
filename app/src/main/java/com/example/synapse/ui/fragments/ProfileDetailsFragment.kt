package com.example.synapse.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.synapse.R
import com.example.synapse.model.data.Subscription
import com.example.synapse.model.res.Stream
import com.example.synapse.network.Resource
import com.example.synapse.ui.adapters.ActiveStreamsAdapter
import com.example.synapse.util.IntentValue
import com.example.synapse.viemodel.ProfileViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileDetailsFragment : Fragment(R.layout.fragment_profile_details), ActiveStreamsAdapter.ActiveStreamOnClick {

    private val TAG = "ProfileDetailsFragment"

    private val profileViewModel : ProfileViewModel by viewModels()
    private lateinit var profileImage : ImageView
    private lateinit var name : TextView
    private lateinit var bio : TextView
    private lateinit var subCount : TextView
    private lateinit var subscriptionsCount : TextView

    private lateinit var incomingProfile : Subscription
    private lateinit var recentVideosRV : RecyclerView

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


            profileViewModel.getRecentStreams(listOf(incomingProfile.id))
            listenToRecentVideos()
        }
    }

    private fun listenToRecentVideos() {
        CoroutineScope(Dispatchers.IO).launch {
            profileViewModel.recentVideos.collect{
                when(it) {
                    is Resource.Success ->{
                        launch(Dispatchers.Main) {
                            val allStreams = it.data!!.streams
                            val adapter = ActiveStreamsAdapter(this@ProfileDetailsFragment, context)
                            adapter.submitList(allStreams)
                            recentVideosRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            recentVideosRV.adapter = adapter
                            Log.d(TAG, "observeActiveStreams: active streams : $allStreams")
                        }
                    }

                    is Resource.Loading ->{
                        launch(Dispatchers.Main){
                            Toast.makeText(activity,"Active Streams Loading...", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is Resource.Error ->{
                        launch(Dispatchers.Main){
                            Toast.makeText(activity,"Error ${it.message}...", Toast.LENGTH_LONG).show()
                        }
                    }

                    is Resource.Idle ->{
                        Log.d(TAG, "observeActiveStreams: All active streams are idle")
                    }
                }
            }
        }
    }

    private fun createView(view: View) {
        profileImage = view.findViewById(R.id.profileDetailsPictureImg)
        name = view.findViewById(R.id.profileDetailsName)
        bio = view.findViewById(R.id.profileDetailsBioTxt)
        subCount = view.findViewById(R.id.profileDetailsSubscriberTxt)
        subscriptionsCount = view.findViewById(R.id.profileDetailsSubscriptionsTxt)
        recentVideosRV = view.findViewById(R.id.profileDetailsRecentVideosRV)
    }

    override fun onStreamClicked(stream: Stream) {
        Toast.makeText(activity, "feature is blocked....", Toast.LENGTH_SHORT).show()
    }
}