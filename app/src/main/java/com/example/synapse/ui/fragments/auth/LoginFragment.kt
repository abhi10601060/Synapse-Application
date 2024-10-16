package com.example.synapse.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.synapse.R
import com.example.synapse.model.req.AuthenticationInput
import com.example.synapse.network.Resource
import com.example.synapse.ui.activities.Authentication
import com.example.synapse.ui.activities.MainActivity
import com.example.synapse.viemodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login_screen) {

    private val TAG = "LoginFragment"

    private lateinit var loginBtn : Button
    private lateinit var signUp : TextView
    private lateinit var  viewModel : AuthViewModel
    private lateinit var emailEdt : EditText
    private lateinit var passEdt : EditText
    private lateinit var progressBar : ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        viewModel = (activity as Authentication).authViewModel
        observeLoginResponse()
        setOnClickListeners()
    }

    private fun createView(view : View) {
        loginBtn = view.findViewById(R.id.login_btn)
        signUp = view.findViewById(R.id.login_signup_text)
        emailEdt = view.findViewById(R.id.login_email_edt)
        passEdt = view.findViewById(R.id.login_pass_edt)
        progressBar = view.findViewById(R.id.login_progress_bar)
    }

    private fun setOnClickListeners() {
        signUp.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        })

        loginBtn.setOnClickListener(View.OnClickListener {
            if (!validUserNameAndPass()) {
                emailEdt.setError("Empty Credentials, Please recheck.")
                return@OnClickListener
            }
            val authInput = AuthenticationInput(emailEdt.text.toString().trim(), passEdt.text.toString().trim())
            viewModel.login(authInput)
        })
    }

    private fun observeLoginResponse(){

        lifecycleScope.launch(Dispatchers.Main){
            viewModel.loginResult.collect{
                when(it){

                    is Resource.Loading ->{
                        progressBar.visibility = View.VISIBLE
                        loginBtn.isClickable = false
                    }

                    is Resource.Error -> {
                        progressBar.visibility = View.GONE
                        loginBtn.isClickable = true
                        emailEdt.requestFocus()
                        emailEdt.setError("Wrong Credentials, Please recheck.")
                    }

                    is Resource.Success ->{
                        progressBar.visibility = View.VISIBLE
                        loginBtn.isClickable = true
                        Log.d(TAG, "observeLoginResponse: ${it.data.toString()}")
                        it.data?.token?.let { tokenStr -> saveUserToken(tokenStr) }
                        openMainActivity()
                    }
                    else ->{

                    }
                }
            }
        }
    }

    private fun validUserNameAndPass() : Boolean{
        val enteredEmail = emailEdt.text.toString().trim()
        val enteredPass = passEdt.text.toString().trim()

        if (enteredPass.equals("") or enteredEmail.equals("")){
            return false
        }
        return true
    }

    private fun saveUserToken(token : String) {
        viewModel.saveUserToken(token)
    }


    private fun openMainActivity() {
        val intent = Intent(activity , MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}