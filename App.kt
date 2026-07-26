package com.nullsec.messenger

import android.app.Application
import com.nullsec.messenger.data.AppRepository
import com.nullsec.messenger.data.ConnectivityObserver
import com.nullsec.messenger.data.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class App : Application() {
    val repository by lazy { AppRepository(this) }
    private val connectivityObserver by lazy { ConnectivityObserver(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        connectivityObserver.start()
        appScope.launch {
            connectivityObserver.online.collect { repository.setConnectionOnline(it) }
        }
    }
}
