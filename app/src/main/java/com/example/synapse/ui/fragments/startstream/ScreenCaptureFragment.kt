package com.example.synapse.ui.fragments.startstream

import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.viemodel.StreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScreenCaptureFragment : Fragment(R.layout.fragment_screen_capture) {
    private val TAG = "ScreenCaptureFragment"

    private val streamViewModel : StreamViewModel by viewModels()

    private lateinit var streamSurfaceViewRenderer: SurfaceViewRenderer
    private lateinit var stopStreamBtn : Button

    private var name : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView(view)
        setOnClicks()

        streamViewModel.init(streamSurfaceViewRenderer,
            activity?.let { LiveKit.create(it.applicationContext) })

        arguments?.let {
            name = it.getString("name")
            Log.d(TAG, "onViewCreated: received name : $name")
            if (name != null) {
                askMediaProjectionPermission()
            }
        }
    }

    private fun setOnClicks() {
        stopStreamBtn.setOnClickListener(View.OnClickListener {
            streamViewModel.stopStream()
        })
    }

    private fun askMediaProjectionPermission() {
        val mediaProjectionManager = activity?.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
        Log.d(TAG, "askMediaProjectionPermission: Permission asked")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            Log.d("LiveKit", "onActivityResult: mediaprojection intent received")
            lifecycleScope.launch {
                name?.let { streamViewModel.startScreenCapture(it, data) }
            }
        }
    }

    private fun createView(view : View) {
        streamSurfaceViewRenderer = view.findViewById(R.id.stream_surface)
        stopStreamBtn = view.findViewById(R.id.screenCaptureStopStreamBtn)
    }
}