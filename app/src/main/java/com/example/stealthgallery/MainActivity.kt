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
            openGallery()
        }

        // Mode awal: Tampilan sistem palsu
        showSystemUI()
    }

    private fun showSystemUI() {
        vaultContainer.visibility = View.GONE
        btnAdd.visibility = View.GONE
        titleView.text = "System Tools"
    }

    private fun showVault() {
        vaultContainer.visibility = View.VISIBLE
        btnAdd.visibility = View.VISIBLE
        titleView.text = "Stealth Vault"
        loadVaultImages()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let { saveFileToVault(it) }
        }
    }

    private fun saveFileToVault(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "vault_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(vaultStorageDir, fileName)
            
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Toast.makeText(this, "Foto disimpan ke brankas!", Toast.LENGTH_SHORT).show()
            loadVaultImages()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVaultImages() {
        val files = vaultStorageDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".png")) } ?: emptyList()
        val adapter = ImageAdapter(this, files)
        vaultGridView.adapter = adapter
    }

    // Adapter untuk menampilkan Grid Gambar
    private inner class ImageAdapter(private val context: Activity, private val files: List<java.io.File>) : BaseAdapter() {
        override fun getCount(): Int = files.size
        override fun getItem(position: Int): Any = files[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView
            val view: View
            
            if (convertView == null) {
                view = LinearLayout(context).apply {
                    layoutParams = GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
                    setPadding(8, 8, 8, 8)
                }
                imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                view.addView(imageView)
            } else {
                view = convertView
                imageView = view.getChildAt(0) as ImageView
            }

            val file = files[position]
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            // Handle klik gambar untuk buka Full Screen
            view.setOnClickListener {
                val intent = Intent(context, FullImageActivity::class.java)
                intent.putExtra("image_path", file.absolutePath)
                context.startActivity(intent)
            }

            return view
        }
    }
}
