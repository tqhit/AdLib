package com.tqhit.adlib.sdk.ext

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

fun TextView.toText(): String {
    return this.text.toString()
}

fun TextView.copyToClipboard() {
    val cm: ClipboardManager = this.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(
        ClipData(
            "text",
            arrayOf("text/plain"),
            ClipData.Item(this.text)
        )
    )
}