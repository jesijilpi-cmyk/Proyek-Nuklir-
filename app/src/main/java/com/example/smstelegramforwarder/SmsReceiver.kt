package com.example.smstelegramforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.displayOriginatingAddress ?: "Unknown"
                val body = message.displayMessageBody ?: ""
                sendToTelegram(context, sender, body)
            }
        }
    }

    private fun sendToTelegram(context: Context, sender: String, body: String) {
        val prefs = context.getSharedPreferences("SmsPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("bot_token", "")
        val chatId = prefs.getString("chat_id", "")

        if (token.isNullOrEmpty() || chatId.isNullOrEmpty()) return

        val text = "📩 SMS Masuk\n👤 Pengirim: $sender\n💬 Pesan: $body"
        val url = "https://api.telegram.org/bot$token/sendMessage"

        val json = "{\"chat_id\":\"$chatId\",\"text\":\"${text.replace("\"", "\\\"").replace("\n", "\\n")}}"
        val bodyRequest = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(bodyRequest)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }
}
