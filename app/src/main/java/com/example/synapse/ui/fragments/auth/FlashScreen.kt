package com.example.synapse.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.synapse.R
import com.example.synapse.ui.activities.Authentication
import com.example.synapse.ui.activities.MainActivity
import com.example.synapse.viemodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlashScreen : Fragment(R.layout.fragment_flash_screen) {

    private lateinit var authViewModel : AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = (activity as Authentication).authViewModel

        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            if (authViewModel.getUserToken() != null){
                openMainActivity()
            }
            else{
                findNavController().navigate(R.id.action_flashScreen_to_loginFragment)
            }

        }
    }

    private fun openMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}