package com.proyek.nuklir

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etToken = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etBotToken)
        val etChatId = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etChatId)
        val switchForward = findViewById<Switch>(R.id.switchForward)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val tvLog = findViewById<TextView>(R.id.tvLog)

        val prefs = getSharedPreferences("nuklir_prefs", Context.MODE_PRIVATE)

        etToken.setText(prefs.getString("bot_token", ""))
        etChatId.setText(prefs.getString("chat_id", ""))
        switchForward.isChecked = prefs.getBoolean("forward_enabled", false)

        checkAndRequestPermissions()

        btnSave.setOnClickListener {
            val token = etToken.text.toString().trim()
            val chatId = etChatId.text.toString().trim()
            val enabled = switchForward.isChecked

            prefs.edit()
                .putString("bot_token", token)
                .putString("chat_id", chatId)
                .putBoolean("forward_enabled", enabled)
                .apply()

            tvLog.append("Konfigurasi disimpan. Status penerusan: $enabled\n")
            Toast.makeText(this, "Disimpan", Toast.LENGTH_SHORT).show()
        }

        switchForward.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("forward_enabled", isChecked).apply()
            tvLog.append("Penerusan SMS diubah: $isChecked\n")
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}
