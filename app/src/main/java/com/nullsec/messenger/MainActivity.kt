package com.nullsec.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nullsec.messenger.ui.NullMessengerApp
import com.nullsec.messenger.ui.theme.NullMessengerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NullMessengerTheme {
                NullMessengerApp()
            }
        }
    }
}
