package com.example.stealthgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var isGalleryMode = false
    private lateinit var container: View
    private lateinit var titleView: TextView
    private lateinit var listView: ListView

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup basic layout programmatically
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        titleView = TextView(this).apply {
            text = "System Tools"
            textSize = 24f
            setPadding(48, 48, 48, 48)
            setOnClickListener { handleSecretTrigger() }
        }

        listView = ListView(this)
        listView.visibility = View.GONE

        root.addView(titleView)
        root.addView(listView)
        setContentView(root)

        container = root
    }

    private fun handleSecretTrigger() {
        // Simple trigger: Tap the title 3 times
        // In a real app, this might be a long press or a specific sequence
        if (!isGalleryMode) {
            Toast.makeText(this, "System Tools v1.0.2", Toast.LENGTH_SHORT).show()
            // For demo purposes, we'll just toggle it on a long click or double tap
            // Let's use a simple toggle for now
            toggleGalleryMode()
        } else {
            toggleGalleryMode()
        }
    }

    // Overriding click listener for triple tap simulation (simplified)
    // In a real implementation, use a GestureDetector
    private var tapCount = 0
    private var lastTapTime: Long = 0

    override fun onUserInteraction() {
        super.onUserInteraction()
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < 300) {
            tapCount++
        } else {
            tapCount = 1
        }
        lastTapTime = currentTime

        if (tapCount == 3) {
            toggleGalleryMode()
            tapCount = 0
        }
    }

    private fun toggleGalleryMode() {
        isGalleryMode = !isGalleryMode
        if (isGalleryMode) {
            titleView.text = "Stealth Gallery"
            listView.visibility = View.VISIBLE
            checkPermissionsAndLoadMedia()
        } else {
            titleView.text = "System Tools"
            listView.visibility = View.GONE
            Toast.makeText(this, "Back to System Tools", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndLoadMedia() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            loadMedia()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMedia()
        } else {
            Toast.makeText(this, "Permission denied. Cannot load media.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMedia() {
        val mediaList = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR " +
                "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"

        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                mediaList.add(cursor.getString(nameColumn))
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mediaList)
        listView.adapter = adapter
        
        if (mediaList.isEmpty()) {
            Toast.makeText(this, "No media found.", Toast.LENGTH_SHORT).show()
        }
    }
}
