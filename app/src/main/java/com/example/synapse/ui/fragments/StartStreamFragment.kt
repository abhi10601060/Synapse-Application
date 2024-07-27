package com.example.synapse.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.example.synapse.R
import com.example.synapse.ui.activities.StartStream
import com.example.synapse.util.IntentLabel
import com.example.synapse.util.IntentValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartStreamFragment : Fragment(R.layout.fragment_start_stream) {

    private lateinit var startLive : RelativeLayout
    private lateinit var startScreenCapture : RelativeLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setOnclicks()
    }

    private fun setOnclicks() {
        startScreenCapture.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, StartStream::class.java)
            intent.putExtra(IntentLabel.START_STREAM_DESTINATION, IntentValue.START_STREAM_SCREEN_CAPTURE)
            startActivity(intent)
        })

        startLive.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, StartStream::class.java)
            intent.putExtra(IntentLabel.START_STREAM_DESTINATION, IntentValue.START_STREAM_LIVE)
            startActivity(intent)
        })
    }

    private fun createView(view: View) {
        startScreenCapture = view.findViewById(R.id.streamStartScreenCaptureParent)
        startLive = view.findViewById(R.id.streamStartLiveParent)

    }
}