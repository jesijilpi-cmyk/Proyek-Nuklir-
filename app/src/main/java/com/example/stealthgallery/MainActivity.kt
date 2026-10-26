package com.example.stealthgallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.stealthgallery.R

class MainActivity : Activity() {
    private lateinit var titleView: TextView
    private lateinit var vaultContainer: View
    private lateinit var vaultGridView: GridView
    private lateinit var btnAdd: Button
    private val vaultStorageDir by lazy { filesDir.parentFile?.resolve("vault_storage") ?: filesDir }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleView = findViewById(R.id.app_title)
        vaultContainer = findViewById(R.id.vault_container)
        vaultGridView = findViewById(R.id.vault_grid_view)
        btnAdd = findViewById(R.id.btn_add_photo)

        // Penyamaran: Judul "System Tools"
        titleView.setOnClickListener(object : View.OnClickListener {
            private var tapCount = 0
            override fun onClick(v: View?) {
                tapCount++
                if (tapCount >= 3) {
                    tapCount = 0
                    showVault()
                }
            }
        })

        btnAdd.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissions(arrayOf(permission), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk akses galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            uri?.let { saveToVault(it) }
        }
    }

    private fun saveToVault(uri: Uri) {
        try {
            if (!vaultStorageDir.exists()) vaultStorageDir.mkdirs()
            
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(vaultStorageDir, fileName)
            
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Toast.makeText(this, "Foto berhasil dikunci!", Toast.LENGTH_SHORT).show()
            showVault()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showVault() {
        vaultContainer.visibility = View.VISIBLE
        loadVaultImages()
    }

    private fun loadVaultImages() {
        val files = vaultStorageDir.listFiles()?.filter { it.isFile } ?: emptyList()
        val imageUris = files.map { Uri.fromFile(it) }

        val adapter = ImageAdapter(this, imageUris)
        vaultGridView.adapter = adapter
    }

    inner class ImageAdapter(private val context: Activity, private val uris: List<Uri>) : BaseAdapter() {
        override fun getCount(): Int = uris.size
        override fun getItem(position: Int): Any = uris[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView = (convertView as? ImageView) ?: ImageView(context).apply {
                layoutParams = GridView.LayoutParams(Gridview_column_width(), Gridview_column_height())
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(4, 4, 4, 4)
            }

            val uri = uris[position]
            try {
                imageView.setImageBitmap(BitmapFactory.decodeFile(java.io.File(uri.path!!).absolutePath))
            } catch (e: Exception) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            imageView.setOnClickListener {
                val intent = Intent(context, FullImageActivity::class.java)
                intent.putExtra("image_uri", uri.toString())
                context.startActivity(intent)
            }

            return imageView
        }

        private fun Gridview_column_width(): Int {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            return screenWidth / 3 - 12
        }

        private fun Gridview_column_height(): Int {
            return (Gridview_column_width() * 1.3).toInt()
        }
    }
}
