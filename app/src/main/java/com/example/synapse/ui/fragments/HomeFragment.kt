package com.example.synapse.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synapse.R
import com.example.synapse.model.data.ProfileDetails
import com.example.synapse.model.data.Subscription
import com.example.synapse.model.res.Stream
import com.example.synapse.network.Resource
import com.example.synapse.ui.activities.WatchStream
import com.example.synapse.ui.adapters.ActiveStreamsAdapter
import com.example.synapse.ui.adapters.SearchedProfileAdapter
import com.example.synapse.util.IntentValue
import com.example.synapse.util.slideDown
import com.example.synapse.viemodel.MainViewModel
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), ActiveStreamsAdapter.ActiveStreamOnClick {

    private val mainViewModel : MainViewModel by viewModels()

    val TAG = "HomeFragment"

    private lateinit var refreshBtn : Button
    private lateinit var activeStreamsRV : RecyclerView
    private lateinit var searchBtn : ImageView
    private lateinit var searchFrame : FrameLayout
    private lateinit var searchBox : EditText
    private lateinit var searchRL : RelativeLayout
    private lateinit var searchCloseBtn : ImageView
    private lateinit var searchTabLayout : TabLayout

    private var searchedStreams : List<Stream> = listOf()
    private var searchedUsers : List<ProfileDetails> = listOf()

    @Inject lateinit var gson: Gson

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setOnClicks()
        observeActiveStreams()
        observeSearchState()
        setUpSearchTablayout()
        observeSearchResults()
    }

    private fun observeSearchResults() {
        lifecycleScope.launch(Dispatchers.Default) {
            mainViewModel.searchStreamsResult.collect{
                when(it){
                    is Resource.Success ->{
                        launch(Dispatchers.Main) {
                            searchTabLayout.visibility = View.VISIBLE

                            val allStreams = it.data!!.streams
                            searchedStreams = allStreams
                            val adapter = ActiveStreamsAdapter(this@HomeFragment, context)
                            adapter.submitList(allStreams)
                            activeStreamsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            activeStreamsRV.adapter = adapter
                            Log.d(TAG, "observeSearchResults: searched streams : $allStreams")
                        }
                    }

                    is Resource.Loading ->{
                        launch(Dispatchers.Main){
                            Toast.makeText(activity,"Searched Streams Loading...", Toast.LENGTH_SHORT).show()
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

        lifecycleScope.launch(Dispatchers.Default) {
            mainViewModel.searchUsersResult.collect{
                when(it){
                    is Resource.Success ->{
                        launch(Dispatchers.Main) {
                            searchedUsers = it.data!!.users
                            Log.d(TAG, "observeSearchResults: searched Users : ${it.data}")
                        }
                    }

                    is Resource.Loading ->{
                        launch(Dispatchers.Main){
                            Toast.makeText(activity,"Searched Streams Loading...", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is Resource.Error ->{
                        launch(Dispatchers.Main){
                            Toast.makeText(activity,"Error ${it.message}...", Toast.LENGTH_LONG).show()
                        }
                    }

                    is Resource.Idle ->{
                        Log.d(TAG, "observeSearchResults: All active streams are idle")
                    }
                }
            }
        }
    }

    private fun setUpSearchTablayout() {
        searchTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(selectedTab: TabLayout.Tab?) {
               selectedTab?.let {
                   Log.d(TAG, "onTabSelected: ${selectedTab.position}")
                   if (it.position == 0){

                       val adapter = ActiveStreamsAdapter(this@HomeFragment, context)
                       adapter.submitList(searchedStreams)
                       activeStreamsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                       activeStreamsRV.adapter = adapter

                       Log.d(TAG, "onTabSelected: streams")
                   }else{

                       val adapter = SearchedProfileAdapter(requireContext(), ::onProfileClick)
                       adapter.submitList(searchedUsers)
                       activeStreamsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                       activeStreamsRV.adapter = adapter

                       Log.d(TAG, "onTabSelected: peoples")
                   }
               }
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

        })
    }

    fun onProfileClick(profile: ProfileDetails){
        val subscription = Subscription(profile.userName, profile.bio, profile.profilePictureUrl, profile.totalSubs, profile.totalStreams, profile.createdOn, profile.streamStatus)
        val bundle = Bundle()
        bundle.putString(IntentValue.INCOMING_PROFILE, gson.toJson(subscription))
        findNavController().navigate(R.id.action_mainMenuHome_to_profileDetailsFragment, bundle)
    }


    private fun observeSearchState() {
        lifecycleScope.launch(Dispatchers.Main) {
            mainViewModel.isSearching.collect{
                if (it){
                    searchFrame.visibility = View.VISIBLE
                    searchRL.slideDown(300,0)
                }
                else{
                    searchFrame.visibility = View.GONE
                }
            }
        }
    }

    private fun observeActiveStreams() {
        mainViewModel.getAllActiveStreams()

         CoroutineScope(Dispatchers.IO).launch {
             mainViewModel.allActiveStreams.collect{
                 when(it) {
                     is Resource.Success ->{
                         launch(Dispatchers.Main) {
                             val allStreams = it.data!!.streams
                             val adapter = ActiveStreamsAdapter(this@HomeFragment, context)
                             adapter.submitList(allStreams)
                             activeStreamsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                             activeStreamsRV.adapter = adapter
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

    private fun setOnClicks() {
        refreshBtn.setOnClickListener(View.OnClickListener {
            mainViewModel.getAllActiveStreams()
        })

        searchBtn.setOnClickListener(View.OnClickListener {
            lifecycleScope.launch {
                mainViewModel.isSearching.emit(true)
            }
        })

        searchCloseBtn.setOnClickListener(View.OnClickListener {
            lifecycleScope.launch {
                mainViewModel.isSearching.emit(false)
                mainViewModel.getAllActiveStreams()
            }
        })

        searchBox.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchBox.windowToken, 0)

                mainViewModel.searchRequest(searchBox.text.toString())
                searchTabLayout.getTabAt(0)?.select()
                return@OnEditorActionListener true
            }
            else{
                return@OnEditorActionListener false
            }
        })
    }

    private fun createView(view : View) {
        refreshBtn = view.findViewById(R.id.homeRefreshBtn)
        activeStreamsRV = view.findViewById(R.id.activeStreamsRV)
        searchBtn = view.findViewById(R.id.homeSearchIcon)
        searchBox = view.findViewById(R.id.homeSearchEdt)
        searchRL = view.findViewById(R.id.homeSearchRL)
        searchFrame = view.findViewById(R.id.homeSearchFrame)
        searchCloseBtn = view.findViewById(R.id.homeSearchCloseImg)
        searchTabLayout = view.findViewById(R.id.homeSearchTabs)
    }

    override fun onStreamClicked(stream: Stream) {
        val gson = Gson()
        val intent = Intent(activity, WatchStream::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("stream" , gson.toJson(stream))
        startActivity(intent)
    }
}