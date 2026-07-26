package com.nullsec.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nullsec.messenger.BuildConfig
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow

private enum class Screen {
    Chats, Contacts, Profile, Settings
}

private data class Conversation(
    val name: String,
    val handle: String,
    val preview: String,
    val unread: Int,
    val online: Boolean
)

private data class Message(
    val author: String,
    val text: String,
    val time: String,
    val mine: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NullMessengerApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Chats) }
    var signedIn by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var stealthMode by rememberSaveable { mutableStateOf(true) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var typing by rememberSaveable { mutableStateOf("") }
    val conversations = remember {
        listOf(
            Conversation("Zero", "@zero", "Waiting for the signal.", 2, true),
            Conversation("Echo", "@echo", "The relay is clean.", 0, true),
            Conversation("Vanta", "@vanta", "Meet at 23:10.", 5, false)
        )
    }
    val messages = remember {
        mutableStateListOf(
            Message("Zero", "Channel online. No logs. No noise.", "09:12", false),
            Message("You", "Null messenger is alive 🔥", "09:13", true),
            Message("Zero", "Keep the vibe dark, keep it clean.", "09:14", false)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050607), Color(0xFF090C0D), Color(0xFF0F1416))
                )
            ),
        color = Color.Transparent
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Null Messenger", fontWeight = FontWeight.Bold)
                            Text(
                                if (BuildConfig.SUPABASE_URL.isBlank()) "Demo mode • Supabase not set" else "Supabase ready • ${BuildConfig.SUPABASE_URL.removePrefix("https://").take(18)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8FA3A0)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF050607),
                        titleContentColor = Color(0xFFF2F6F4)
                    ),
                    actions = {
                        AssistChip(
                            onClick = { stealthMode = !stealthMode },
                            label = { Text(if (stealthMode) "Stealth" else "Visible") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (stealthMode) Color(0xFF0F1717) else Color(0xFF1C1616),
                                labelColor = Color(0xFF68F0B2)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF060809)) {
                    BottomItem(Screen.Chats, screen, Icons.Filled.ChatBubbleOutline) { screen = it }
                    BottomItem(Screen.Contacts, screen, Icons.Filled.Home) { screen = it }
                    BottomItem(Screen.Profile, screen, Icons.Filled.PersonOutline) { screen = it }
                    BottomItem(Screen.Settings, screen, Icons.Filled.Settings) { screen = it }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                if (!signedIn) {
                    AuthCard(
                        email = email,
                        password = password,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onSignIn = { signedIn = true },
                        onSignUp = { signedIn = true }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                when (screen) {
                    Screen.Chats -> ChatsScreen(conversations, messages, typing, onTyping = { typing = it }) {
                        if (typing.isNotBlank()) {
                            messages.add(Message("You", typing.trim(), "Now", true))
                            typing = ""
                        }
                    }
                    Screen.Contacts -> ContactsScreen(conversations)
                    Screen.Profile -> ProfileScreen(signedIn, notifications, stealthMode, email) { notifications = it }
                    Screen.Settings -> SettingsScreen(notifications, stealthMode) {
                        notifications = it
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthCard(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Connect to the grid", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Login shell with space for Supabase auth.", color = Color(0xFF91A5A0))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSignIn) { Text("Sign in") }
                OutlinedButton(onClick = onSignUp) { Text("Sign up") }
            }
        }
    }
}

@Composable
private fun ChatsScreen(
    conversations: List<Conversation>,
    messages: List<Message>,
    typing: String,
    onTyping: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Encrypted") })
            AssistChip(onClick = {}, label = { Text("Offline cache") })
            AssistChip(onClick = {}, label = { Text("NullSec mode") })
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                conversations.forEach { chat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chat.name, fontWeight = FontWeight.SemiBold)
                            Text(chat.preview, color = Color(0xFF91A5A0), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (chat.unread > 0) {
                                Text("${chat.unread} new", color = Color(0xFF68F0B2), fontWeight = FontWeight.Bold)
                            }
                            Text(if (chat.online) "online" else "away", color = Color(0xFF91A5A0))
                        }
                    }
                    if (chat != conversations.last()) Divider(color = Color(0xFF1C2528))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current thread", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(
                    modifier = Modifier.height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = typing,
                        onValueChange = onTyping,
                        label = { Text("Type a message") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onSend) {
                        Icon(Icons.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (message.mine) Color(0xFF163228) else Color(0xFF131A1C))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(message.author, color = Color(0xFF68F0B2), fontWeight = FontWeight.Bold)
            Text(message.text)
            Text(message.time, color = Color(0xFF91A5A0), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ContactsScreen(conversations: List<Conversation>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            conversations.forEach { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(contact.name, fontWeight = FontWeight.SemiBold)
                        Text(contact.handle, color = Color(0xFF91A5A0))
                    }
                    Text(if (contact.online) "LIVE" else "IDLE", color = Color(0xFF68F0B2))
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    signedIn: Boolean,
    notifications: Boolean,
    stealthMode: Boolean,
    email: String,
    onNotificationsChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (signedIn) "Signed in as ${if (email.isBlank()) "anon@null" else email}" else "Guest session active")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications", modifier = Modifier.weight(1f))
                Switch(checked = notifications, onCheckedChange = onNotificationsChange)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stealth mode", modifier = Modifier.weight(1f))
                Checkbox(checked = stealthMode, onCheckedChange = null)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    notifications: Boolean,
    stealthMode: Boolean,
    onNotificationsChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1416)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Backend key loaded: ${BuildConfig.SUPABASE_ANON_KEY.take(14)}…", color = Color(0xFF91A5A0))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications", modifier = Modifier.weight(1f))
                Switch(checked = notifications, onCheckedChange = onNotificationsChange)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stealth mode", modifier = Modifier.weight(1f))
                Checkbox(checked = stealthMode, onCheckedChange = null)
            }
        }
    }
}

@Composable
private fun BottomItem(
    item: Screen,
    current: Screen,
    icon: ImageVector,
    onSelect: (Screen) -> Unit
) {
    NavigationBarItem(
        selected = current == item,
        onClick = { onSelect(item) },
        icon = { Icon(icon, contentDescription = item.name) },
        label = { Text(item.name) }
    )
}
