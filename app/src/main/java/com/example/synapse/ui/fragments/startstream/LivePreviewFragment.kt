package com.example.synapse.ui.fragments.startstream

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.synapse.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivePreviewFragment : Fragment(R.layout.fragment_live_preview) {
    lateinit var startStreamBtn : Button
    lateinit var titleEdt : EditText
    lateinit var navController : NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        setOnClicks()

        navController = findNavController()

    }

    private fun setOnClicks() {
        startStreamBtn.setOnClickListener(View.OnClickListener {
            val text = titleEdt.text.toString()
            val bundle = Bundle()
            bundle.putString("name", text)
            navController.navigate(R.id.action_livePreviewFragment_to_liveFragment, bundle)
        })
    }

    private fun createView(view: View) {
        startStreamBtn = view.findViewById(R.id.liveDetailStartStreamBtn)
        titleEdt = view.findViewById(R.id.liveDetailTitleEdt)
    }
}