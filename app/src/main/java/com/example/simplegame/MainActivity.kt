package com.example.simplegame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var targetNumber = Random.nextInt(1, 101)
    private var attempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val titleText = TextView(this).apply {
            text = "Tebak Angka (1 - 100)\n"
            textSize = 20f
        }

        val hintText = TextView(this).apply {
            text = "Masukkan tebakan Anda di bawah ini:"
            textSize = 16f
        }

        val inputField = EditText(this).apply {
            hint = "Angka 1-100"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val submitButton = Button(this).apply {
            text = "Tebak"
        }

        val resultText = TextView(this).apply {
            text = ""
            textSize = 16f
            setPadding(0, 20, 0, 0)
        }

        submitButton.setOnClickListener {
            val inputStr = inputField.text.toString()
            if (inputStr.isNotEmpty()) {
                val guess = inputStr.toInt()
                attempts++
                when {
                    guess < targetNumber -> resultText.text = "Terlalu kecil! Percobaan ke-$attempts"
                    guess > targetNumber -> resultText.text = "Terlalu besar! Percobaan ke-$attempts"
                    else -> {
                        resultText.text = "🎉 Benar! Angkanya $targetNumber. Total percobaan: $attempts"
                        targetNumber = Random.nextInt(1, 101)
                        attempts = 0
                    }
                }
            }
        }

        layout.addView(titleText)
        layout.addView(hintText)
        layout.addView(inputField)
        layout.addView(submitButton)
        layout.addView(resultText)

        setContentView(layout)
    }
}
