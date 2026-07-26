package com.nullmessenger

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isMine: Boolean,
    val time: String
)

class AppRepository {

    fun initialMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                text = "Null Messenger online.",
                isMine = false,
                time = now()
            ),
            ChatMessage(
                text = "Messages stay on this device.",
                isMine = false,
                time = now()
            )
        )
    }

    fun send(
        current: List<ChatMessage>,
        text: String
    ): List<ChatMessage> {
        return current + ChatMessage(
            text = text,
            isMine = true,
            time = now()
        )
    }

    private fun now(): String {
        return SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date())
    }
}
