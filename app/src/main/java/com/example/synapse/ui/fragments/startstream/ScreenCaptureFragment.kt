package com.example.synapse.ui.fragments.startstream

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.synapse.R
import com.example.synapse.viemodel.StreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer

@AndroidEntryPoint
class ScreenCaptureFragment : Fragment(R.layout.fragment_screen_capture) {
    private val TAG = "ScreenCaptureFragment"

    private val streamViewModel : StreamViewModel by viewModels()
    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)

        streamViewModel.init(streamSurfaceViewRenderer,
            activity?.let { LiveKit.create(it.applicationContext) })

        arguments?.let {
            val name = it.getString("name")
            Log.d(TAG, "onViewCreated: received name : $name")
            if (name != null) {
                streamViewModel.startScreenCapture(name)
            }
        }
    }

    private fun createView(view : View) {
        streamSurfaceViewRenderer = view.findViewById(R.id.stream_surface)
    }
}