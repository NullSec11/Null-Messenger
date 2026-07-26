package com.nullsec.messenger.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nullsec.messenger.workers.SyncQueueWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AppViewModel(private val repo: AppRepository) : ViewModel() {
    private val _screen = MutableStateFlow<AppScreen>(if (repo.auth.value.currentUser == null) AppScreen.Splash else AppScreen.Home)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    val auth = repo.auth
    val users = repo.users
    val threads = repo.threads
    val messages = repo.messages
    val settings = repo.settings
    val online = repo.connectionOnline

    init {
        scheduleSyncWork()
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(repo.auth, repo.connectionOnline) { auth, connected ->
                auth.currentUser to connected
            }.collect { (user, connected) ->
                if (user != null && connected) repo.togglePresence(true)
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1400)
            if (repo.auth.value.currentUser == null) _screen.value = AppScreen.Login
            else _screen.value = AppScreen.Home
        }
    }

    fun login(username: String, password: String): Result<Unit> {
        val result = repo.login(username, password.toCharArray())
        if (result.isSuccess) _screen.value = AppScreen.Home
        return result
    }

    fun register(username: String, password: String): Result<Unit> {
        val result = repo.register(username, password.toCharArray())
        if (result.isSuccess) _screen.value = AppScreen.Home
        return result
    }

    fun goRegister() { _screen.value = AppScreen.Register }
    fun goLogin() { _screen.value = AppScreen.Login }
    fun goHome() { _screen.value = AppScreen.Home }
    fun goNewChat() { _screen.value = AppScreen.NewChat }
    fun goProfile() { _screen.value = AppScreen.Profile }
    fun goSettings() { _screen.value = AppScreen.Settings }

    fun openThread(threadId: String) { _screen.value = AppScreen.Chat(threadId) }
    fun createThreadAndOpen(username: String) { openThread(repo.createThread(username).id) }
    fun threadFor(threadId: String): ChatThread? = repo.getThread(threadId)

    fun onBack() {
        _screen.value = when (_screen.value) {
            AppScreen.Register, AppScreen.Login -> AppScreen.Login
            AppScreen.NewChat, AppScreen.Profile, AppScreen.Settings, is AppScreen.Chat -> AppScreen.Home
            AppScreen.Home, AppScreen.Splash -> AppScreen.Home
        }
    }

    fun sendMessage(threadId: String, text: String) = repo.sendMessage(threadId, text)
    fun deleteMessage(id: String) = repo.deleteMessageLocally(id)
    fun markRead(threadId: String) = repo.markThreadRead(threadId)
    fun logout() { repo.logout(); _screen.value = AppScreen.Login }
    fun deleteAccount() { repo.deleteAccount(); _screen.value = AppScreen.Login }
    fun updateSettings(settings: PrivacySettings) = repo.updateSettings(settings)
    fun togglePresence(online: Boolean) = repo.setConnectionOnline(online)

    private fun scheduleSyncWork() {
        val request = PeriodicWorkRequestBuilder<SyncQueueWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(repo.context).enqueueUniquePeriodicWork(
            "null_messenger_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        fun factory(repo: AppRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppViewModel::class.java)) return AppViewModel(repo) as T
                error("Unsupported ViewModel class")
            }
        }
    }
}
