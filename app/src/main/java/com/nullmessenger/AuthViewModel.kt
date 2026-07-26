package com.nullmessenger


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {


private val _isLoading = MutableStateFlow(false)
val isLoading: StateFlow<Boolean> = _isLoading

private val _error = MutableStateFlow<String?>(null)
val error: StateFlow<String?> = _error

private val _isLoggedIn = MutableStateFlow(false)
val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

fun signUp(email: String, password: String) {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            _error.value = null

            supabase.auth.signUpWith(
                email = email,
                password = password
            )

            _isLoggedIn.value = true

        } catch (e: Exception) {
            _error.value = e.message ?: "Registration failed"
        } finally {
            _isLoading.value = false
        }
    }
}

fun signIn(email: String, password: String) {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            _error.value = null

            supabase.auth.signInWith(
                email = email,
                password = password
            )

            _isLoggedIn.value = true

        } catch (e: Exception) {
            _error.value = e.message ?: "Login failed"
        } finally {
            _isLoading.value = false
        }
    }
}

fun signOut() {
    viewModelScope.launch {
        try {
            supabase.auth.signOut()
            _isLoggedIn.value = false
        } catch (e: Exception) {
            _error.value = e.message ?: "Logout failed"
        }
    }
}



}

