package com.example.synapse.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.synapse.R
import com.example.synapse.model.data.ProfileDetails
import com.example.synapse.model.res.ProfileDetailsOutPut
import com.example.synapse.network.Resource
import com.example.synapse.ui.activities.Authentication
import com.example.synapse.viemodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val TAG = "ProfileFragment"
    private lateinit var profileViewModel : ProfileViewModel

    private lateinit var editProfile : ImageView
    private lateinit var editProfileImageOptions : RelativeLayout
    private lateinit var editProfileClose : ImageView
    private lateinit var editProfileDone : ImageView
    private lateinit var editBio : ImageView
    private lateinit var editBioOptions : RelativeLayout
    private lateinit var editBioClose : ImageView
    private lateinit var editBioDone : ImageView

    private lateinit var userName : TextView
    private lateinit var profilePic : ImageView
    private lateinit var userBio : TextView
    private lateinit var userBioEdt : EditText
    private lateinit var subsCount : TextView
    private lateinit var logoutBtn : ImageView

    private lateinit var  resultLauncher : ActivityResultLauncher<Intent>
    private lateinit var changedImageBase64 : String
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel : ProfileViewModel by viewModels()
        profileViewModel = viewModel

        createView(view)
        listenToEditStates()
        listenToProfileDetailsOutput()
        listenToUpdateProfilePicOutput()
        listenToUpdateBioOutput()
        registerResultLauncher()
        setOnClicks()

        profileViewModel.getOwnProfileDetails()
    }

    private fun listenToUpdateProfilePicOutput() {
        lifecycleScope.launch(Dispatchers.Main) {
            profileViewModel.updateProfilePicOutPut.collect(){res ->
                when(res){
                    is Resource.Loading ->{
                        Toast.makeText(context, "loading update pic", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error ->{
                        Toast.makeText(context, "Error in updating profile picture ", Toast.LENGTH_LONG).show()
                        loadImageFromProfileDetails()
                    }

                    is Resource.Success ->{
                        Log.d(TAG, "listenToProfileDetailsOutput: ${res.data}")
                        Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_LONG).show()
                        profileViewModel.closeEditImageState()
                        loadImageFromProfileDetails()
                    }

                    else ->{

                    }
                }
            }
        }
    }

    private fun listenToUpdateBioOutput() {
        lifecycleScope.launch(Dispatchers.Main) {
            profileViewModel.updateBioOutPut.collect(){res ->
                when(res){
                    is Resource.Loading ->{
                        Toast.makeText(context, "loading update bio", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error ->{
                        Toast.makeText(context, "Error in updating bio ", Toast.LENGTH_LONG).show()
                    }

                    is Resource.Success ->{
                        Log.d(TAG, "listenToProfileDetailsOutput: ${res.data}")
                        Toast.makeText(context, "Bio updated successfully", Toast.LENGTH_LONG).show()
                        userBio.text = userBioEdt.text.toString()
                    }

                    else ->{

                    }
                }
            }
        }
    }

    private fun listenToProfileDetailsOutput() {
        lifecycleScope.launch(Dispatchers.Main) {
            profileViewModel.profileDetails.collect(){res ->
                when(res){
                    is Resource.Loading ->{
                        Toast.makeText(context, "loading Profile Details", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error ->{
                        Toast.makeText(context, "Error in getting profile details", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Success ->{
                        Log.d(TAG, "listenToProfileDetailsOutput: ${res.data}")
                        res.data?.let { displayProfileDetails(it.profileDetails)
                        saveDetailsToSahredPref(it.profileDetails)}
                    }
                    else ->{
                        Toast.makeText(context, "else block", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveDetailsToSahredPref(profileDetails: ProfileDetails) {
        profileViewModel.saveDetails(profileDetails)
    }

    private fun displayProfileDetails(profileDetails: ProfileDetails) {
        userName.text = profileDetails.userName
        userBio.text = profileDetails.bio
        loadImageFromProfileDetails()
        subsCount.text = profileDetails.totalSubs
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private fun setOnClicks() {
        editBio.setOnClickListener(View.OnClickListener {
            profileViewModel.onEditBioState()
        })

        editBioClose.setOnClickListener(View.OnClickListener {
            profileViewModel.closeEditBioState()
        })

        editBioDone.setOnClickListener(View.OnClickListener {
            profileViewModel.closeEditBioState()
            if (!userBio.text.equals(userBioEdt.text.toString())){
                Log.d(TAG, "setOnClicks: updating bio to : ${userBioEdt.text.toString()}")
                profileViewModel.updateBio(userBioEdt.text.toString())
            }
        })

        editProfile.setOnClickListener(View.OnClickListener {
            profileViewModel.onEditImageState()
            pickImage()
        })

        editProfileClose.setOnClickListener(View.OnClickListener {
            profileViewModel.closeEditImageState()
            loadImageFromProfileDetails()
        })

        editProfileDone.setOnClickListener(View.OnClickListener {
            profileViewModel.updateProfilePic(changedImageBase64)
            profileViewModel.closeEditImageState()
        })

        logoutBtn.setOnClickListener(View.OnClickListener {
            showConfirmLogoutDialog()
        })
    }

    private fun showConfirmLogoutDialog() {
        val confirmDialog = AlertDialog.Builder(context)
            .setTitle("Are you sure, you want to logout?")
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, i ->
                dialog.dismiss()
            })
            .setPositiveButton("yes", DialogInterface.OnClickListener { dialog, i ->
                profileViewModel.logout()
                val intent = Intent(activity, Authentication::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            })
        confirmDialog.show()
    }

    private fun loadImageFromProfileDetails() {
        val res = profileViewModel.profileDetails.value
        if (res is Resource.Success){
            val profilePicUrl = res.data?.profileDetails?.profilePictureUrl
            context?.let { Glide.with(it).load(profilePicUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(profilePic) }
        }
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
                    userBio.visibility = View.GONE
                    userBioEdt.visibility = View.VISIBLE
                    userBioEdt.setText(userBio.text.toString())
                }
                else{
                    editBio.visibility = View.VISIBLE
                    editBioOptions.visibility = View.GONE
                    userBio.visibility = View.VISIBLE
                    userBioEdt.visibility = View.GONE
                }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private fun pickImage(){
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        resultLauncher.launch(intent)
    }
    private fun registerResultLauncher(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            try {
                val uri = result.data?.data
                val picturePath =getRealPathFromUri(uri!!)
                Log.d(TAG, "registerResultLauncher: receivedUri: ${uri} and real path: $picturePath")
                val thumbnailSize = 240
                val thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picturePath),thumbnailSize, thumbnailSize)
                profilePic.setImageBitmap(thumbnail)
                val base64Image = getBase64FromBitmap(thumbnail)
                changedImageBase64 = base64Image
//                Log.d(TAG, "registerResultLauncher: base64Image : $base64Image")
            }catch (e : Exception){
                Log.d(TAG, "registerResultLauncher: eexception : ${e}")
            }
        }
    }

    @SuppressLint("Recycle")
    private fun getRealPathFromUri(uri : Uri) : String{
        var result : String? = null
        val projection : Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context?.contentResolver?.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(projection.get(0))
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    private fun getBase64FromBitmap(bitmap : Bitmap) : String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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

        userBio = view.findViewById(R.id.profileBioTxt)
        userBioEdt = view.findViewById(R.id.profileBioEditText)
        profilePic = view.findViewById(R.id.profilePictureImg)
        userName = view.findViewById(R.id.profileName)
        subsCount = view.findViewById(R.id.profileSubscriberTxt)
        logoutBtn = view.findViewById(R.id.profileLogoutImg)
    }
}