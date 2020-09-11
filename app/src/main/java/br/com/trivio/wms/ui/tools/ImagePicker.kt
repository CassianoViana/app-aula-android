package br.com.trivio.wms.ui.tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException

object ImagePicker {

  private const val DEFAULT_MIN_WIDTH_QUALITY = 400
  private const val TAG = "ImagePicker"
  private const val TEMP_IMAGE_NAME = "tempImage"
  var minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY

  fun getImageFromResult(
    context: Context, resultCode: Int,
    data: Intent?
  ): Bitmap? {
    Log.d(TAG, "getImageFromResult, resultCode: $resultCode")
    var bm: Bitmap? = null
    val imageFile = getTempFile(context)

    if (resultCode == Activity.RESULT_OK) {
      val uri = data?.data
      uri?.let{

      }

      val isCamera = data == null
        || uri == null
        || uri == Uri.fromFile(imageFile)

      val selectedImage: Uri? = if (isCamera) {
        Uri.fromFile(imageFile)
      } else {
        data?.data
      }

      Log.d(TAG, "selectedImage: $selectedImage")
      selectedImage?.let {
        bm = getImageResized(context, selectedImage)
        val rotation = getRotation(context, selectedImage, isCamera)
        bm = rotate(bm, rotation)
      }
    }
    return bm
  }

  private fun decodeBitmap(context: Context, theUri: Uri, sampleSize: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inSampleSize = sampleSize
    val fileDescriptor: AssetFileDescriptor?
    var actuallyUsableBitmap: Bitmap? = null
    try {
      fileDescriptor = context.contentResolver.openAssetFileDescriptor(theUri, "r")
      actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
        fileDescriptor.fileDescriptor, null, options
      )
      Log.d(
        TAG, options.inSampleSize.toString() + " sample method bitmap ... " +
          actuallyUsableBitmap.width + " " + actuallyUsableBitmap.height
      )
      return actuallyUsableBitmap
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }
    return actuallyUsableBitmap
  }

  /**
   * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
   */
  private fun getImageResized(context: Context, selectedImage: Uri): Bitmap? {
    var bitmap: Bitmap?
    val sampleSizes = intArrayOf(5, 3, 2, 1)
    var i = 0
    do {
      bitmap = decodeBitmap(context, selectedImage, sampleSizes[i])
      if (bitmap == null) {
        break
      }
      Log.d(TAG, "resizer: new bitmap width = " + bitmap.width)
      i++
    } while (bitmap != null && bitmap.width < minWidthQuality && i < sampleSizes.size)
    return bitmap
  }

  private fun getRotation(context: Context, imageUri: Uri, isCamera: Boolean): Int {
    val rotation: Int = if (isCamera) {
      getRotationFromCamera(context, imageUri)
    } else {
      getRotationFromGallery(context, imageUri)
    }
    Log.d(TAG, "Image rotation: $rotation")
    return rotation
  }

  private fun getRotationFromCamera(context: Context, imageFile: Uri): Int {
    var rotate = 0
    try {
      context.contentResolver.notifyChange(imageFile, null)
      imageFile.path?.let { path ->
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
          ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
          ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
          ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return rotate
  }

  fun getRotationFromGallery(context: Context, imageUri: Uri): Int {
    val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)
    val cursor = context.contentResolver.query(imageUri, columns, null, null, null) ?: return 0
    cursor.moveToFirst()
    val orientationColumnIndex = cursor.getColumnIndex(columns[0])
    val int = cursor.getInt(orientationColumnIndex)
    cursor.close()
    return int
  }

  public fun rotate(bm: Bitmap?, rotation: Int): Bitmap? {
    if (rotation != 0) {
      val matrix = Matrix()
      matrix.postRotate(rotation.toFloat())
      return if (bm != null) Bitmap.createBitmap(
        bm,
        0,
        0,
        bm.width,
        bm.height,
        matrix,
        true
      ) else null
    }
    return bm
  }

  fun getTempFile(context: Context): File {
    val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
    imageFile.parentFile.mkdirs()
    return imageFile
  }
}
