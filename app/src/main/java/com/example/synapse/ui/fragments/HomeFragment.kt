package com.example.synapse.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synapse.R
import com.example.synapse.model.res.Stream
import com.example.synapse.network.Resource
import com.example.synapse.ui.activities.WatchStream
import com.example.synapse.ui.adapters.ActiveStreamsAdapter
import com.example.synapse.viemodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), ActiveStreamsAdapter.ActiveStreamOnClick {

    private val mainViewModel : MainViewModel by viewModels()

    val TAG = "HomeFragment"

    private lateinit var refreshBtn : Button
    private lateinit var activeStreamsRV : RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setOnClicks()
        observeActiveStreams()
    }

    private fun observeActiveStreams() {
         CoroutineScope(Dispatchers.IO).launch {
             mainViewModel.allActiveStreams.collect{
                 when(it) {
                     is Resource.Success ->{
                         launch(Dispatchers.Main) {
                             val allStreams = it.data!!.streams
                             val adapter = ActiveStreamsAdapter(this@HomeFragment)
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
    }

    private fun createView(view : View) {
        refreshBtn = view.findViewById(R.id.homeRefreshBtn)
        activeStreamsRV = view.findViewById(R.id.activeStreamsRV)
    }

    override fun onStreamClicked(stream: Stream) {
        val intent = Intent(activity, WatchStream::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("name" , stream.name)
        startActivity(intent)
    }
}