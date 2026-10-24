package com.mythos.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
        private const val PREFS_NAME = "SmsForwarderPrefs"
        private const val KEY_TARGET_URL = "target_url"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages != null) {
                val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val targetUrl = sharedPreferences.getString(KEY_TARGET_URL, "")

                if (!targetUrl.isNullOrEmpty()) {
                    for (sms in messages) {
                        val sender = sms.displayOriginatingAddress
                        val body = sms.messageBody
                        Log.d(TAG, "SMS dari $sender: $body")
                        forwardSms(targetUrl, sender, body)
                    }
                }
            }
        }
    }

    private fun forwardSms(urlString: String, sender: String, message: String) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true

                val jsonInputString = "{\"sender\": \"$sender\", \"message\": \"$message\"}"
                val os: OutputStream = conn.outputStream
                val input = jsonInputString.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                Log.d(TAG, "Penerusan SMS berhasil, respon: $responseCode")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal meneruskan SMS", e)
            }
        }
    }
}
