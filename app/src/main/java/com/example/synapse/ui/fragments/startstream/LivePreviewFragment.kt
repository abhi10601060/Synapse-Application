package com.example.synapse.ui.fragments.startstream

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.synapse.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File

@AndroidEntryPoint
class LivePreviewFragment : Fragment(R.layout.fragment_live_preview) {

    private val TAG ="LivePreviewFragment"

    private lateinit var startStreamBtn : Button
    private lateinit var browseThumbnailBtn : Button
    private lateinit var thumbNailPreview : ImageView
    private lateinit var thumbnailPath : TextView
    private lateinit var titleEdt : EditText
    private lateinit var descEdt : EditText
    private lateinit var tagsEdt : EditText
    private lateinit var navController : NavController
    private lateinit var  resultLauncher : ActivityResultLauncher<Intent>
    private var thumbNailBase64 : String? = ""
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createView(view)
        setOnClicks()
        registerResultLauncher()

        navController = findNavController()

    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private fun setOnClicks() {
        startStreamBtn.setOnClickListener(View.OnClickListener {
            if (titleEdt.text.toString().isNullOrBlank()){
                titleEdt.error = "title can not be empty"
                return@OnClickListener
            }
            val text = titleEdt.text.toString()
            val bundle = Bundle()
            bundle.putString("title", text)
            bundle.putString("thumbnail", thumbNailBase64)
            bundle.putString("desc", descEdt.text.toString())
            bundle.putString("tags", tagsEdt.text.toString())
            navController.navigate(R.id.action_livePreviewFragment_to_liveFragment, bundle)
        })

        browseThumbnailBtn.setOnClickListener(View.OnClickListener {
            pickImage()
        })
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private fun pickImage(){
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        resultLauncher.launch(intent)
    }
    private fun registerResultLauncher(){
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            try {
                val uri = result.data?.data
                val picturePath =getRealPathFromUri(uri!!)
                Log.d(TAG, "registerResultLauncher: receivedUri: ${uri} and real path: $picturePath")
                val thumbnailSize = 480
                val thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picturePath),thumbnailSize, thumbnailSize)
                thumbnailPath.text = picturePath
                thumbNailPreview.setImageBitmap(thumbnail)
                thumbNailBase64 = getBase64FromBitmap(thumbnail)
                Log.d(TAG, "registerResultLauncher: base64Image : $thumbNailBase64")
            }catch (e : Exception){
                Log.d(TAG, "registerResultLauncher: eexception : ${e}")
            }
        }
    }

    private fun getBase64FromBitmap(bitmap : Bitmap) : String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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

    private fun createView(view: View) {
        titleEdt = view.findViewById(R.id.livePreviewStreamTitleEdt)
        tagsEdt = view.findViewById(R.id.livePreviewTagEdt)
        descEdt = view.findViewById(R.id.livePreviewDescEdt)
        startStreamBtn = view.findViewById(R.id.livePreviewStartStreamBtn)
        browseThumbnailBtn = view.findViewById(R.id.livePreviewBrowseBtn)
        thumbNailPreview = view.findViewById(R.id.liveStreamThumbnailImg)
        thumbnailPath =view.findViewById(R.id.livePreviewThumbnailPathTxt) 
    }
}