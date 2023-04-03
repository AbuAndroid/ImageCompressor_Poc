package com.example.filesize_poc.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.filesize_poc.utils.Utils.getFile
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

class ImageCompression {

    fun compressImage(filePath: String?, context: Context?): String {
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        //      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        //      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = actualWidth.toFloat() / actualHeight
        val maxRatio = maxWidth / maxHeight
        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false
        //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            Log.e("ImageCompression", "compressImage: ${exception.printStackTrace()}")
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            Log.e("ImageCompression", "compressImage: ${exception.printStackTrace()}")
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        //      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate(180f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            Log.e("ImageCompression", "compressImage: ${e.printStackTrace()}")
            e.printStackTrace()
        }

        val filename: String? = getFile(context!!,).absolutePath

        try {
            val out = FileOutputStream(filename)
            //          write the compressed bitmap at the destination specified by filename.
            scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            Log.e("ImageCompression", "compressImage: ${e.printStackTrace()}")
            e.printStackTrace()
        }
        return filename ?: ""
    }

//    fun checkImageOrientation(filePath: String?, context: FragmentActivity?): String {
//        val filename: String =
//            com.digivalsolutions.module.core.utils.file.getFile(context).absolutePath
//        try {
//            var scaledBitmap: Bitmap? = null
//            val options = BitmapFactory.Options()
//            options.inJustDecodeBounds = true
//            var bmp = BitmapFactory.decodeFile(filePath, options)
//            var actualHeight = options.outHeight
//            var actualWidth = options.outWidth
//            val maxHeight = 816.0f
//            val maxWidth = 612.0f
//            var imgRatio = actualWidth.toFloat() / actualHeight
//            val maxRatio = maxWidth / maxHeight
//            //      width and height values are set maintaining the aspect ratio of the image
//            if (actualHeight > maxHeight || actualWidth > maxWidth) {
//                if (imgRatio < maxRatio) {
//                    imgRatio = maxHeight / actualHeight
//                    actualWidth = (imgRatio * actualWidth).toInt()
//                    actualHeight = maxHeight.toInt()
//                } else if (imgRatio > maxRatio) {
//                    imgRatio = maxWidth / actualWidth
//                    actualHeight = (imgRatio * actualHeight).toInt()
//                    actualWidth = maxWidth.toInt()
//                } else {
//                    actualHeight = maxHeight.toInt()
//                    actualWidth = maxWidth.toInt()
//                }
//            }
//            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
//            //      inJustDecodeBounds set to false to load the actual bitmap
//            options.inJustDecodeBounds = false
//            //      this options allow android to claim the bitmap memory if it runs low on memory
//            options.inPurgeable = true
//            options.inInputShareable = true
//            options.inTempStorage = ByteArray(16 * 1024)
//            try {
////          load the bitmap from its path
//                bmp = BitmapFactory.decodeFile(filePath, options)
//            } catch (exception: OutOfMemoryError) {
//                exception.printStackTrace()
//            }
//            try {
//                scaledBitmap =
//                    Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
//            } catch (exception: OutOfMemoryError) {
//                exception.printStackTrace()
//            }
//            val ratioX = actualWidth / options.outWidth.toFloat()
//            val ratioY = actualHeight / options.outHeight.toFloat()
//            val middleX = actualWidth / 2.0f
//            val middleY = actualHeight / 2.0f
//            val scaleMatrix = Matrix()
//            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
//            val canvas = Canvas(scaledBitmap!!)
//            canvas.setMatrix(scaleMatrix)
//            canvas.drawBitmap(
//                bmp,
//                middleX - bmp.width / 2,
//                middleY - bmp.height / 2,
//                Paint(Paint.FILTER_BITMAP_FLAG)
//            )
//            //      check the rotation of the image and display it properly
//            val exif: ExifInterface
//            try {
//                exif = ExifInterface(filePath!!)
//                val orientation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
//                )
//                Log.d("EXIF", "Exif: $orientation")
//                val matrix = Matrix()
//                when (orientation) {
//                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
//                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
//                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
//                        matrix.setRotate(180f)
//                        matrix.postScale(-1f, 1f)
//                    }
//                    ExifInterface.ORIENTATION_TRANSPOSE -> {
//                        matrix.setRotate(90f)
//                        matrix.postScale(-1f, 1f)
//                    }
//                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
//                    ExifInterface.ORIENTATION_TRANSVERSE -> {
//                        matrix.setRotate(-90f)
//                        matrix.postScale(-1f, 1f)
//                    }
//                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
//                }
//                scaledBitmap = Bitmap.createBitmap(
//                    scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix,
//                    true
//                )
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            try {
//                val out = FileOutputStream(filename)
//                //          write the compressed bitmap at the destination specified by filename.
//                scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            }
//        } catch (exp: Exception) {
//            Log.e("TAG", "compressImage: ", exp)
//        }
//        return filename
//    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = heightRatio.coerceAtMost(widthRatio)
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}