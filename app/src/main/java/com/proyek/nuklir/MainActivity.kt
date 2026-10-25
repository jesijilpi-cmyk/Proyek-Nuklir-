package com.proyek.nuklir

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var coreTemp = 300
    private var controlRodsInserted = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnToggleRods = findViewById<Button>(R.id.btnToggleRods)
        val btnCoolDown = findViewById<Button>(R.id.btnCoolDown)

        fun updateUI() {
            tvStatus.text = "Suhu Reaktor: $coreTemp °C\nBatang Kendali: ${if (controlRodsInserted) "Masuk (Aman)" else "Keluar (Aktif)"}"
        }

        updateUI()

        btnToggleRods.setOnClickListener {
            controlRodsInserted = !controlRodsInserted
            coreTemp = if (controlRodsInserted) 300 else 850
            updateUI()
            Toast.makeText(this, "Status batang kendali diubah", Toast.LENGTH_SHORT).show()
        }

        btnCoolDown.setOnClickListener {
            coreTemp = maxOf(100, coreTemp - 50)
            updateUI()
            Toast.makeText(this, "Sistem pendingin darurat aktif", Toast.LENGTH_SHORT).show()
        }
    }
}
