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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

/**
 * STEALTH VAULT: Brankas Foto/Video Tersembunyi dengan Tampilan Grid Gambar
 */
class MainActivity : AppCompatActivity() {
    private var isVaultOpen = false
    private lateinit var rootLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var vaultContainer: LinearLayout
    private lateinit var gridView: GridView
    private lateinit var btnAddMedia: Button
    private val vaultFolder: File
        get() = File(filesDir, "vault_storage")
    private var tapCount = 0
    private var lastTapTime: Long = 0

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveToVault(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleView = findViewById(R.id.title_view)
        vaultContainer = findViewById(R.id.vault_container)
        gridView = findViewById(R.id.grid_view)
        btnAddMedia = findViewById(R.id.btn_add_media)
        btnAddMedia.setOnClickListener { checkPermissionAndOpenGallery() }
        titleView.setOnClickListener { handleTripleTap() }
    }

    private fun handleTripleTap() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime > 1000) {
            tapCount = 0
        }
        tapCount++
        lastTapTime = currentTime
        if (tapCount == 3) {
            toggleVault()
            tapCount = 0
        }
    }

    private fun toggleVault() {
        isVaultOpen = !isVaultOpen
        if (isVaultOpen) {
            titleView.text = "Secure Vault"
            vaultContainer.visibility = View.VISIBLE
            loadVaultImages()
            Toast.makeText(this, "Vault Dibuka", Toast.LENGTH_SHORT).show()
        } else {
            titleView.text = "System Tools"
            vaultContainer.visibility = View.GONE
            Toast.makeText(this, "Vault Dikunci", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndOpenGallery() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(perm), 102)
        }
    }

    private fun saveToVault(sourceUri: Uri) {
        try {
            val fileName = "vault_${System.currentTimeMillis()}.jpg"
            val targetFile = File(vaultFolder, fileName)
            vaultFolder.mkdirs()
            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Foto berhasil dimasukkan ke brankas!", Toast.LENGTH_SHORT).show()
            loadVaultImages()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVaultImages() {
        val files = vaultFolder.listFiles { file -> file.isFile && file.name.endsWith(".jpg") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
        val adapter = ImageAdapter(files)
        gridView.adapter = adapter
        gridView.setOnItemClickListener { _, _, position, _ ->
            val file = files[position]
            val intent = Intent(this, FullImageActivity::class.java).apply {
                putExtra("image_path", file.absolutePath)
            }
            startActivity(intent)
        }
    }

    private inner class ImageAdapter(private val imageFiles: List<File>) : BaseAdapter() {
        override fun getCount(): Int = imageFiles.size
        override fun getItem(position: Int): Any = imageFiles[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView = convertView as? ImageView ?: ImageView(this@MainActivity).apply {
                layoutParams = AbsListView.LayoutParams(300, 300)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(4, 4, 4, 4)
            }
            val file = imageFiles[position]
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)
            return imageView
        }
    }
}
