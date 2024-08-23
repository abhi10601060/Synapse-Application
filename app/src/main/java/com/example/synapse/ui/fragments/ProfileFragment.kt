package com.example.synapse.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.synapse.R
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.network.Resource
import com.example.synapse.viemodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val TAG = "ProfileFragment"
    private val profileViewModel : ProfileViewModel by viewModels()

    private lateinit var editProfile : ImageView
    private lateinit var editProfileImageOptions : RelativeLayout
    private lateinit var editProfileClose : ImageView
    private lateinit var editProfileDone : ImageView
    private lateinit var editBio : ImageView
    private lateinit var editBioOptions : RelativeLayout
    private lateinit var editBioClose : ImageView
    private lateinit var editBioDone : ImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        listenToEditStates()
        listenToProfileDetailsOutput()
        setOnClicks()

        profileViewModel.addAbhiData()

        profileViewModel.getOwnProfileDetails()
    }

    private fun listenToProfileDetailsOutput() {
        lifecycleScope.launch(Dispatchers.Main) {
            profileViewModel.profileDetails.collect(){res ->
                when(res){
                    is Resource.Loading ->{
                        Toast.makeText(context, "loading", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error ->{
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Success ->{
                        Log.d(TAG, "listenToProfileDetailsOutput: ${res.data}")
//                        displayProfileDetails(res)
                    }
                    else ->{
                        Toast.makeText(context, "else block", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

//    private fun displayProfileDetails(res: Resource.Success<ProfileDetailsOutPut>) {
//        TODO("Not yet implemented")
//    }


    private fun setOnClicks() {
        editBio.setOnClickListener(View.OnClickListener {
            profileViewModel.onEditBioState()
        })

        editBioClose.setOnClickListener(View.OnClickListener {
            profileViewModel.closeEditBioState()
        })

        editProfile.setOnClickListener(View.OnClickListener {
            profileViewModel.onEditImageState()
        })

        editProfileClose.setOnClickListener(View.OnClickListener {
            profileViewModel.closeEditImageState()
        })
    }

    private fun listenToEditStates() {
        lifecycleScope.launch(Dispatchers.Main){
            profileViewModel.editImageState.collect(){isOn ->
                if (isOn){
                    editProfile.visibility = View.GONE
                    editProfileImageOptions.visibility = View.VISIBLE

                }
                else{
                    editProfile.visibility = View.VISIBLE
                    editProfileImageOptions.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main){
            profileViewModel.editBioState.collect(){isOn ->
                if (isOn){
                    editBio.visibility = View.GONE
                    editBioOptions.visibility = View.VISIBLE
                }
                else{
                    editBio.visibility = View.VISIBLE
                    editBioOptions.visibility = View.GONE
                }
            }
        }
    }

    private fun createView(view: View) {
        editProfile = view.findViewById(R.id.profileImageEditImg)
        editBioOptions = view.findViewById(R.id.profileEditBioOptionsRL)
        editProfileClose = view.findViewById(R.id.editImageClose)
        editProfileDone = view.findViewById(R.id.editImageDone)
        editProfileImageOptions = view.findViewById(R.id.profileEditImageOptionsRL)
        editBio = view.findViewById(R.id.profileEditBioImg)
        editBioClose = view.findViewById(R.id.editBioClose)
        editBioDone = view.findViewById(R.id.editBioDone)
    }
}