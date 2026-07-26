package com.nullmessenger

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {

    private val _messages =
        MutableStateFlow(repository.initialMessages())

    val messages: StateFlow<List<ChatMessage>> =
        _messages.asStateFlow()

    private val _draft =
        MutableStateFlow("")

    val draft: StateFlow<String> =
        _draft.asStateFlow()

    fun onDraftChange(value: String) {
        _draft.value = value
    }

    fun sendMessage() {
        val text = _draft.value.trim()

        if (text.isEmpty()) {
            return
        }

        _messages.value = repository.send(
            _messages.value,
            text
        )

        _draft.value = ""
    }
}
