package com.proyek.nuklir

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etToken: EditText
    private lateinit var etChatId: EditText
    private lateinit var switchForward: Switch
    private lateinit var tvLogs: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etToken = findViewById(R.id.etBotToken)
        etChatId = findViewById(R.id.etChatId)
        switchForward = findViewById(R.id.switchForward)
        tvLogs = findViewById(R.id.tvLogs)

        switchForward.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAndRequestPermissions()
            } else {
                appendLog("Penerusan SMS dinonaktifkan.")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        val needed = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
        } else {
            appendLog("Izin SMS telah diberikan.")
        }
    }

    private fun appendLog(msg: String) {
        val current = tvLogs.text.toString()
        tvLogs.text = "$current\n$msg"
    }
}
