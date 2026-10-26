package com.example.stealthgallery

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import com.example.stealthgallery.R

class FullImageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageView = findViewById<ImageView>(R.id.fullImageView)
        val imagePath = intent.getStringExtra("image_path")

        if (imagePath != null) {
            val bitmap = android.graphics.BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        }
    }
}
