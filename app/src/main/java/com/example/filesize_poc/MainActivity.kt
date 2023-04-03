package com.example.filesize_poc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.filesize_poc.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var tempImageUri:Uri? = null
    private var tempImageFilePath = ""
    private var sizeString = ""

    private val compressbeforeSize:TextView by lazy {
        findViewById(R.id.uiTvCompressedSize)
    }

    private val compressImageView:ImageView by lazy {
        findViewById(R.id.uiIvCompressedImge)
    }

    private val requestMultiplePermissions by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted){
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val requestToTakePicture by lazy {
        registerForActivityResult(ActivityResultContracts.TakePicture()) {sucecss->
            if(sucecss){
                compressImage(filesDir.absolutePath,1.5)
                compressbeforeSize.text=sizeString
                setImage(imageView = compressImageView, filePath = filesDir.absolutePath)
            }
        }
    }

    private val requestToGetPicture by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) {uri->
            uriToFile(this, uri = uri!!,"select_image_from_gallery")?.let {file->
                compressImage(filePath = file.absolutePath,0.5)
                compressbeforeSize.text=sizeString

                setImage(imageView = compressImageView, filePath = file.absolutePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMultiplePermissions.launch(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA
            )
        )
        requestToTakePicture
        requestToGetPicture
        setContentView(binding.root)
        setUpListeners()
    }


    private fun setUpListeners() {
        binding.uibtGetpickture.setOnClickListener {
            requestToGetPicture.launch("image/*")
        }

        binding.uiBtTakePickture.setOnClickListener {
            tempImageUri = FileProvider.getUriForFile(this,"com.example.filesize_poc",createImageFile().also {
                tempImageFilePath = it.absolutePath
            })
            requestToTakePicture.launch(tempImageUri)
        }
    }

    private fun createImageFile(fileName:String="temp_image"):File{
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg",storageDir)
    }

    private fun uriToFile(context: Context,uri: Uri,fileName: String): File? {
        context.contentResolver.openInputStream(uri)?.let {inputStream ->
            val tempFile = createImageFile(fileName)
            val fileOutputStream = FileOutputStream(tempFile)
            inputStream.copyTo(fileOutputStream)
            inputStream.close()
            fileOutputStream.close()

            return tempFile
        }
        return null
    }

    private fun compressImage(filePath:String,targetMB:Double = 1.0){

        sizeString = ""

        var image:Bitmap = BitmapFactory.decodeFile(filePath)
        val exif = ExifInterface(filePath)
        val exifOrientation : Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION ,ExifInterface.ORIENTATION_NORMAL
        )
        val exifDegree: Int = exifOrientationDegrees(exifOrientation)

        image = rotateImage(image,exifDegree.toFloat())

        try {
            val fileSizeIMb = getFileSizeInMb(filePath)
            sizeString += "  sizebefore : ${String.format("%.2f",fileSizeIMb)} MB"
            var quality = 100
            if(fileSizeIMb > targetMB){
                quality = ((targetMB/fileSizeIMb)*100).toInt()
            }
            val fileOutputStream = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.JPEG,quality,fileOutputStream)
            fileOutputStream.close()
            sizeString += "  sizeafter: ${getFileSizeInMb(filePath)} MB"
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getFileSizeInMb(filePath: String):Double{
        val file = File(filePath)
        val length = file.length()

        val fileSizeInKb = (length/1024).toString().toDouble()
        val fileSizeInMb = (fileSizeInKb/1024).toString().toDouble()

        return fileSizeInMb
    }

    private fun rotateImage(source: Bitmap, angel: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angel)

        return Bitmap.createBitmap(source,0,0,source.width,source.height,matrix,true)
    }

    private fun setImage(imageView: ImageView,filePath: String){
        Log.e("path",filePath)
        Glide.with(imageView.context)
            .asBitmap().load(filePath)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imageView)
    }

    private fun exifOrientationDegrees(exifOrientation: Int): Int {
        return when(exifOrientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }

            else -> 0
        }
    }

}