package com.example.photoeditor

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class OpenGallery : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var flipVertical: Button
    lateinit var flipHorizontal: Button
    lateinit var details: Button
    lateinit var save: Button
    lateinit var detailsTextView: TextView

    private var originalBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery)
        imageView = findViewById(R.id.selectedImage)
        flipVertical = findViewById(R.id.flipVertical)
        flipHorizontal = findViewById(R.id.flipHorizontal)
        details = findViewById(R.id.detail)
        save = findViewById(R.id.save)
        detailsTextView = findViewById(R.id.detailsTextView)


        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        imageView.setImageURI(imageUri)

        if (imageUri != null) {
            originalBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            currentBitmap = originalBitmap
            imageView.setImageBitmap(currentBitmap)
        }

        details.setOnClickListener {
            val imageDetails = getImageDetails(imageUri)
            detailsTextView.text = imageDetails

        }
        save.setOnClickListener {
            if (currentBitmap != null) {
                saveImage(currentBitmap!!)
            }
        }
        flipVertical.setOnClickListener {
            if (originalBitmap != null && currentBitmap != null) {
                currentBitmap = flipBitmapVertically(currentBitmap!!)
                imageView.setImageBitmap(currentBitmap)
            }
        }
        flipHorizontal.setOnClickListener {
            if (originalBitmap != null && currentBitmap != null) {
                currentBitmap = flipBitmapHorizontal(currentBitmap!!)
                imageView.setImageBitmap(currentBitmap)
            }
        }

    }

    private fun flipBitmapVertically(bitmap: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postScale(1F, -1F)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipBitmapHorizontal(bitmap: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postScale(-1F, 1F)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveImage(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val contentValue = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val contentResolver = contentResolver
        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
        imageUri?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getImageDetails(imageUri: Uri): String {
        val contentResolver: ContentResolver = contentResolver
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE
        )
        val cursor: Cursor? = contentResolver.query(imageUri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val width = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val height = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val size = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                return "Image Details:\nName: $displayName\nDimensions: $width x $height\nSize: $size bytes"
            }
        }
        return "Image details not found"
    }

}
