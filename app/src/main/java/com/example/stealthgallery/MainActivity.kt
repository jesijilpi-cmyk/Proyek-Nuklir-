package com.example.stealthgallery

import android.Manifest
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

    private var tapCount = 0
    private var lastTapTime: Long = 0

    private val vaultFolder by lazy { File(filesDir, "vault_storage") }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveToVault(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!vaultFolder.exists()) {
            vaultFolder.mkdirs()
        }

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        titleView = TextView(this).apply {
            text = "System Tools"
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }

        titleView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime < 400) {
                tapCount++
            } else {
                tapCount = 1
            }
            lastTapTime = currentTime

            if (tapCount == 3) {
                toggleVault()
                tapCount = 0
            }
        }

        vaultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        btnAddMedia = Button(this).apply {
            text = "+ Tambah Foto ke Brankas"
            setOnClickListener {
                checkPermissionAndOpenGallery()
            }
        }

        gridView = GridView(this).apply {
            numColumns = 3
            horizontalSpacing = 10
            verticalSpacing = 10
            setPadding(0, 20, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        rootLayout.addView(titleView)
        vaultContainer.addView(btnAddMedia)
        vaultContainer.addView(gridView)
        rootLayout.addView(vaultContainer)

        setContentView(rootLayout)
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
    }

    private inner class ImageAdapter(private val imageFiles: List<File>) : BaseAdapter() {
        override fun getCount(): Int {
            return imageFiles.size
        }

        override fun getItem(position: Int): Any {
            return imageFiles[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val imageView: ImageView = if (convertView == null) {
                ImageView(this@MainActivity).apply {
                    layoutParams = AbsListView.LayoutParams(300, 300)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(4, 4, 4, 4)
                }
            } else {
                convertView as ImageView
            }

            val file = imageFiles[position]
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView.setImageBitmap(bitmap)

            return imageView
        }
    }
}
