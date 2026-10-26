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
import android.widget.*

class MainActivity : Activity() {

    private lateinit var appTitle: TextView
    private lateinit var vaultContainer: LinearLayout
    private lateinit var btnAddPhoto: Button
    private lateinit var vaultGridView: GridView

    private var isVaultOpen = false
    private val vaultStorageDir = java.io.File(filesDir, "vault_storage")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appTitle = findViewById(R.id.app_title)
        vaultContainer = findViewById(R.id.vault_container)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        vaultGridView = findViewById(R.id.vault_grid_view)

        if (!vaultStorageDir.exists()) {
            vaultStorageDir.mkdirs()
        }

        appTitle.setOnClickListener {
            var tapCount = 0
            appTitle.setOnClickListener {
                tapCount++
                if (tapCount == 3) {
                    openVault()
                    tapCount = 0
                }
            }
        }

        // Perbaikan: Tetapkan listener sekali saja untuk menghindari rekursi
        var tapCount = 0
        appTitle.setOnClickListener {
            tapCount++
            if (tapCount == 3) {
                openVault()
                tapCount = 0
            }
        }

        btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 1001)
        }

        refreshVault()
    }

    private fun openVault() {
        isVaultOpen = true
        vaultContainer.visibility = View.VISIBLE
        appTitle.text = "Stealth Vault"
    }

    private fun refreshVault() {
        val files = vaultStorageDir.listFiles()?.filter { 
            it.name.endsWith(".jpg") || it.name.endsWith(".png") || it.name.endsWith(".jpeg") 
        } ?: emptyList()

        val adapter = ImageAdapter(this, files)
        vaultGridView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return
            saveFileToVault(uri)
        }
    }

    private fun saveFileToVault(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val outputFile = java.io.File(vaultStorageDir, fileName)
            
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Foto tersimpan aman!", Toast.LENGTH_SHORT).show()
            refreshVault()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ImageAdapter(
        private val context: Activity,
        private val images: List<java.io.File>
    ) : BaseAdapter() {
        override fun getCount(): Int = images.size
        override fun getItem(position: Int): Any = images[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView
            val view: View

            if (convertView == null) {
                view = ImageView(context).apply {
                    layoutParams = GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        400
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(8, 8, 8, 8)
                }
                imageView = view as ImageView
                view.setOnClickListener {
                    val file = images[position]
                    val intent = Intent(context, FullImageActivity::class.java).apply {
                        putExtra("file_path", file.absolutePath)
                    }
                    context.startActivity(intent)
                }
                view = view
            } else {
                view = convertView
                imageView = view as ImageView
                view.setOnClickListener {
                    val file = images[position]
                    val intent = Intent(context, FullImageActivity::class.java).apply {
                        putExtra("file_path", file.absolutePath)
                    }
                    context.startActivity(intent)
                }
            }

            val file = images[position]
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            return view
        }
    }
}
