package com.example.synapse.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
class SignupFragment : Fragment(R.layout.fragment_signup_screen){

    private lateinit var signUpBtn : Button
    private lateinit var emailEdt : EditText
    private lateinit var passEdt : EditText
    private lateinit var confirmPassEdt : EditText
    private lateinit var viewModel: AuthViewModel
    private lateinit var progressBar : ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as Authentication).authViewModel

        createView(view)
        setOnClickListeners()
        observeSignupResponse()
    }

    private fun createView(view: View) {
        signUpBtn = view.findViewById(R.id.signup_btn)
        emailEdt = view.findViewById(R.id.signup_email_edt)
        passEdt = view.findViewById(R.id.signup_pass_edt)
        confirmPassEdt = view.findViewById(R.id.signup_confirm_pass_edt)
        progressBar = view.findViewById(R.id.signup_progress_bar)
    }

    private fun setOnClickListeners() {
        signUpBtn.setOnClickListener(View.OnClickListener {
            val res = validUserNameAndPass();
            if (res == 1) {
                emailEdt.setError("Empty Credentials, Please recheck.")
                return@OnClickListener
            }
            if (res == 2) {
                confirmPassEdt.setError("Confirm password doesn't match")
                return@OnClickListener
            }
            val authInput =
                AuthenticationInput(emailEdt.text.toString().trim(), confirmPassEdt.text.toString().trim())
            viewModel.signup(authInput)
        })
    }

    private fun observeSignupResponse(){

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.signupResult.collect{
                when(it){

                    is Resource.Loading ->{
                        progressBar.visibility = View.VISIBLE
                        signUpBtn.isClickable = false
                    }

                    is Resource.Error -> {
                        progressBar.visibility = View.GONE
                        signUpBtn.isClickable = true
                        emailEdt.setError("User exists already.")
                    }

                    is Resource.Success ->{
                        if (it.data?.message.equals("user exists already" , ignoreCase = true))
                        progressBar.visibility = View.VISIBLE
                        signUpBtn.isClickable = true
                        it.data?.let { it1 -> saveUser(it1.token) }
                        openMainActivity()
                    }

                    else ->{

                    }
                }
            }
        }
    }

    private fun validUserNameAndPass() : Int{
        val enteredEmail = emailEdt.text.toString().trim()
        val enteredPass = passEdt.text.toString().trim()
        val enteredConfirmPass = confirmPassEdt.text.toString().trim()

        if (enteredPass.equals("") or enteredEmail.equals("") or enteredConfirmPass.equals("")) return 1
        if(!enteredPass.equals(enteredConfirmPass)) return 2
        return 0
    }

    private fun saveUser(token : String) {
        viewModel.saveUserToken(token)
    }
    private fun openMainActivity() {
        val intent = Intent(activity , MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}