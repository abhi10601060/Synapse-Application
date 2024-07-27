package com.example.synapse.ui.fragments.startstream

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.synapse.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivePreviewFragment : Fragment(R.layout.fragment_live_preview) {
    private lateinit var livePreviewText : TextView
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        setOnClicks()

        navController = findNavController()

    }

    private fun setOnClicks() {
        livePreviewText.setOnClickListener(View.OnClickListener {
            navController.navigate(R.id.action_livePreviewFragment_to_liveFragment)
        })
    }

    private fun createView(view: View) {
        livePreviewText = view.findViewById(R.id.livePreviewText)
    }
}