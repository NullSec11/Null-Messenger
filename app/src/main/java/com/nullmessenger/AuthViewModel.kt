package com.nullmessenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank()) {
            _error.value = "Email is required"
            return
        }

        if (username.isBlank()) {
            _error.value = "Username is required"
            return
        }

        if (password.length < 8) {
            _error.value = "Password must be at least 8 characters"
            return
        }

        if (!password.any { it.isUpperCase() }) {
            _error.value = "Password must contain an uppercase letter"
            return
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            _error.value = "Password must contain a special character"
            return
        }

        if (password != confirmPassword) {
            _error.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                onSuccess()

            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
