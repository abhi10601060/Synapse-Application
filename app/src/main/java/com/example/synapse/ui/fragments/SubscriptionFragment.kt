package com.example.synapse.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synapse.R
import com.example.synapse.model.data.Subscription
import com.example.synapse.network.Resource
import com.example.synapse.ui.adapters.SubscriptionsAdapter
import com.example.synapse.util.IntentValue
import com.example.synapse.viemodel.MainViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionFragment : Fragment(R.layout.fragment_subscription) {

    private val mainViewModel : MainViewModel by viewModels()

    private lateinit var subscriptionRV : RecyclerView
    private val gson = Gson()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        listenTOSubscriptions()
    }

    private fun listenTOSubscriptions() {
        mainViewModel.getAllSubscriptions()

        lifecycleScope.launch(Dispatchers.Main) {
            mainViewModel.subscriptions.collect{
                when(it){
                    is Resource.Success ->{
                        val adapter = context?.let { SubscriptionsAdapter(it, ::onProfileClick) }
                        adapter?.submitList(it.data?.subscriptions)
                        subscriptionRV.adapter = adapter
                        subscriptionRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    }

                    is Resource.Loading ->{
                        Toast.makeText(context, "Loading subscriptions", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error ->{
                        Toast.makeText(context, "Error loading subscriptions", Toast.LENGTH_SHORT).show()
                    }

                    else ->{

                    }
                }
            }
        }
    }

    fun onProfileClick(subscription: Subscription){
        val bundle = Bundle()
        bundle.putString(IntentValue.INCOMING_PROFILE, gson.toJson(subscription))
        findNavController().navigate(R.id.action_mainMenuSubs_to_profileDetailsFragment, bundle)
    }

    private fun createView(view: View) {
        subscriptionRV = view.findViewById(R.id.subscriptionsProfileRV)
    }
}