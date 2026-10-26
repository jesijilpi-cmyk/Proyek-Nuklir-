package com.example.stealthgallery

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

/**
 * STEALTH VAULT: Brankas Foto/Video Tersembunyi
 * Fitur:
 * 1. Penyamaran: Tampilan default "System Tools".
 * 2. Vault: Foto/Video disalin ke folder privat internal (app-specific).
 * 3. Keamanan: File tidak akan terlihat di Gallery/Google Photos biasa.
 * 4. Akses: Klik 3x pada judul untuk masuk ke Vault.
 */
class MainActivity : AppCompatActivity() {

    private var isVaultOpen = false
    private lateinit var rootLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var vaultContainer: LinearLayout
    private lateinit var mediaListView: ListView
    private lateinit var btnAddMedia: Button

    private var tapCount = 0
    private var lastTapTime: Long = 0

    // Folder internal untuk menyimpan file secara privat
    private val vaultFolder by lazy { File(filesDir, "vault_storage") }

    // Handler untuk memilih media dari Galeri/File Picker
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveToVault(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi folder vault
        if (!vaultFolder.exists()) {
            vaultFolder.mkdirs()
        }

        // Layout Utama (Programmatic)
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        titleView = TextView(this).apply {
            text = "System Tools"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }

        // Listener ketukan 3x untuk membuka Vault
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

        // Container untuk Vault (Hidden by default)
        vaultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        btnAddMedia = Button(this).apply {
            text = "Add Photo/Video to Vault"
            setOnClickListener {
                // Periksa izin sebelum memilih
                val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (perms.all { ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED }) {
                    pickMediaLauncher.launch("*/*")
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, perms, 101)
                }
            }
        }

        mediaListView = ListView(this)

        // Susun UI
        rootLayout.addView(titleView)
        vaultContainer.addView(btnAddMedia)
        vaultContainer.addView(mediaListView)
        rootLayout.addView(vaultContainer)
        setContentView(rootLayout)
    }

    private fun toggleVault() {
        isVaultOpen = !isVaultOpen
        if (isVaultOpen) {
            titleView.text = "Secure Vault"
            vaultContainer.visibility = View.VISIBLE
            loadVaultMedia()
            Toast.makeText(this, "Vault Unlocked", Toast.LENGTH_SHORT).show()
        } else {
            titleView.text = "System Tools"
            vaultContainer.visibility = View.GONE
            Toast.makeText(this, "Vault Locked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToVault(sourceUri: Uri) {
        try {
            val contentResolver = this.contentResolver
            val fileName = "vault_${System.currentTimeMillis()}"
            
            // Mencoba menebak tipe MIME
            val mimeType = contentResolver.getType(sourceUri) ?: "application/octet-stream"
            val extension = when {
                mimeType.contains("image") -> "jpg"
                mimeType.contains("video") -> "mp4"
                else -> "dat"
            }
            val targetFile = File(vaultFolder, "$fileName.$extension")

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(this, "Saved to Vault!", Toast.LENGTH_SHORT).show()
            loadVaultMedia() // Refresh list
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVaultMedia() {
        val files = vaultFolder.listFiles()
        val fileNames = files?.map { it.name } ?: listOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        mediaListView.adapter = adapter
        
        if (fileNames.isEmpty()) {
            Toast.makeText(this, "Vault is empty", Toast.LENGTH_SHORT).show()
        }
    }
}
