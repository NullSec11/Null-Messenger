package com.nullsec.messenger.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider as M3Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nullsec.messenger.App
import com.nullsec.messenger.data.AppScreen
import com.nullsec.messenger.data.AppViewModel
import com.nullsec.messenger.data.ChatMessage
import com.nullsec.messenger.data.ChatThread
import com.nullsec.messenger.data.MessageCrypto
import com.nullsec.messenger.data.MessageStatus
import com.nullsec.messenger.data.PrivacySettings
import com.nullsec.messenger.ui.components.NullSecLogo
import com.nullsec.messenger.ui.theme.NullBlack
import com.nullsec.messenger.ui.theme.NullCyan
import com.nullsec.messenger.ui.theme.NullMuted
import com.nullsec.messenger.ui.theme.NullRed
import com.nullsec.messenger.ui.theme.NullSurface
import com.nullsec.messenger.ui.theme.NullSurface2
import com.nullsec.messenger.ui.theme.NullText

@Composable
fun NullMessengerRoot() {
    val context = LocalContext.current
    val vm: AppViewModel = viewModel(factory = AppViewModel.factory((context.applicationContext as App).repository))
    val screen by vm.screen.collectAsStateWithLifecycle()
    val auth by vm.auth.collectAsStateWithLifecycle()
    val threads by vm.threads.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val users by vm.users.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val online by vm.online.collectAsStateWithLifecycle()

    BackHandler(enabled = screen != AppScreen.Home && screen != AppScreen.Splash) {
        vm.onBack()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = NullBlack) {
        when (val current = screen) {
            AppScreen.Splash -> SplashScreen()
            AppScreen.Login -> LoginScreen(
                onLogin = vm::login,
                onGoRegister = vm::goRegister,
            )
            AppScreen.Register -> RegisterScreen(
                onRegister = vm::register,
                onGoLogin = vm::goLogin,
            )
            AppScreen.Home -> HomeScreen(
                username = auth.currentUser?.username,
                threads = threads,
                online = online,
                onOpenThread = vm::openThread,
                onNewChat = vm::goNewChat,
                onProfile = vm::goProfile,
                onSettings = vm::goSettings,
                onTogglePresence = vm::togglePresence,
            )
            AppScreen.NewChat -> NewChatScreen(
                currentUsername = auth.currentUser?.username,
                users = users,
                onSelectUser = vm::createThreadAndOpen,
                onBack = vm::goHome,
            )
            is AppScreen.Chat -> ChatScreen(
                thread = vm.threadFor(current.threadId),
                messages = messages.filter { it.threadId == current.threadId },
                currentUsername = auth.currentUser?.username.orEmpty(),
                online = online,
                settings = settings,
                onSend = { vm.sendMessage(current.threadId, it) },
                onDeleteLocal = vm::deleteMessage,
                onBack = vm::goHome,
                onMarkRead = { vm.markRead(current.threadId) },
            )
            AppScreen.Profile -> ProfileScreen(
                username = auth.currentUser?.username,
                onLogout = vm::logout,
                onDeleteAccount = vm::deleteAccount,
                onBack = vm::goHome,
            )
            AppScreen.Settings -> SettingsScreen(
                settings = settings,
                online = online,
                onToggleSetting = vm::updateSettings,
                onBack = vm::goHome,
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        NullSecLogo()
    }
}

@Composable
private fun LoginScreen(onLogin: (String, String) -> Result<Unit>, onGoRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NullSecLogo()
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            error = onLogin(username, password).exceptionOrNull()?.message
        }) { Text("Login") }
        Spacer(Modifier.height(8.dp))
        TextButtonish("Create account", onGoRegister)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun RegisterScreen(onRegister: (String, String) -> Result<Unit>, onGoLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NullSecLogo()
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Unique username") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { error = onRegister(username, password).exceptionOrNull()?.message }) { Text("Register") }
        Spacer(Modifier.height(8.dp))
        TextButtonish("Back to login", onGoLogin)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun HomeScreen(
    username: String?,
    threads: List<ChatThread>,
    online: Boolean,
    onOpenThread: (String) -> Unit,
    onNewChat: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onTogglePresence: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Null Messenger", color = NullText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(username?.let { "@$it" } ?: "Guest", color = NullMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (online) "online" else "offline", color = if (online) NullCyan else NullMuted)
                Spacer(Modifier.width(8.dp))
                Switch(checked = online, onCheckedChange = onTogglePresence)
            }
        }
        Spacer(Modifier.height(12.dp))
        NullSecLogo(Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AssistChip(onClick = onNewChat, label = { Text("New chat") }, leadingIcon = { Icon(Icons.Rounded.Add, null) })
            AssistChip(onClick = onProfile, label = { Text("Profile") }, leadingIcon = { Icon(Icons.Rounded.AccountCircle, null) })
            AssistChip(onClick = onSettings, label = { Text("Settings") }, leadingIcon = { Icon(Icons.Rounded.Settings, null) })
        }
        Spacer(Modifier.height(16.dp))
        Text("Chats", color = NullMuted)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(threads) { thread ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenThread(thread.id) },
                    colors = CardDefaults.cardColors(containerColor = NullSurface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("@${thread.peerUsername}", color = NullText, fontWeight = FontWeight.Bold)
                            Text(formatTime(thread.lastMessageAt), color = NullMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(thread.lastMessagePreview.ifBlank { "No messages yet" }, color = NullMuted)
                        if (thread.unreadCount > 0) Text("${thread.unreadCount} unread", color = NullCyan)
                    }
                }
            }
        }
        if (threads.isEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("No chats yet — start one with a username.", color = NullMuted)
        }
    }
}

@Composable
private fun NewChatScreen(
    currentUsername: String?,
    users: List<com.nullsec.messenger.data.AppUser>,
    onSelectUser: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val results = users.filter { it.username.contains(query, ignoreCase = true) && it.username != currentUsername }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = NullText) }
            Text("New chat", color = NullText, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(query, { query = it }, label = { Text("Search username") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { user ->
                Card(Modifier.fillMaxWidth().clickable { onSelectUser(user.username) }, colors = CardDefaults.cardColors(containerColor = NullSurface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("@${user.username}", color = NullText, fontWeight = FontWeight.Bold)
                        Text(if (user.isOnline) "online" else "offline", color = NullMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    thread: ChatThread?,
    messages: List<ChatMessage>,
    currentUsername: String,
    online: Boolean,
    settings: PrivacySettings,
    onSend: (String) -> Unit,
    onDeleteLocal: (String) -> Unit,
    onBack: () -> Unit,
    onMarkRead: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    LaunchedEffect(thread?.id) { onMarkRead() }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = NullText) }
            Column {
                Text("@${thread?.peerUsername ?: "chat"}", color = NullText, fontWeight = FontWeight.Bold)
                Text(if (online) "connected" else "offline queue active", color = if (online) NullCyan else NullMuted)
            }
        }
        M3Divider(color = NullSurface2)
        LazyColumn(
            modifier = Modifier.weight(1f).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val mine = msg.senderUsername == currentUsername
                val body = if (msg.isDeletedLocally) "Message deleted locally" else MessageCrypto.decrypt(msg.encryptedBody)
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                    Card(colors = CardDefaults.cardColors(containerColor = if (mine) NullSurface2 else NullSurface)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(body, color = NullText)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatTime(msg.createdAt), color = NullMuted)
                                Spacer(Modifier.width(8.dp))
                                Text(statusLabel(msg.status), color = NullMuted)
                                Spacer(Modifier.width(8.dp))
                                TextButtonish("Delete", onClick = { onDeleteLocal(msg.id) })
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message with emoji…") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                onSend(text)
                text = ""
            }) { Text("Send") }
        }
    }
}

@Composable
private fun ProfileScreen(
    username: String?,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = NullText) }
            Text("Profile", color = NullText, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        NullSecLogo(Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
        Text("@${username ?: "guest"}", color = NullText)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogout) { Icon(Icons.Rounded.Logout, null, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Logout") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDeleteAccount) { Icon(Icons.Rounded.Delete, null, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Delete account") }
    }
}

@Composable
private fun SettingsScreen(
    settings: PrivacySettings,
    online: Boolean,
    onToggleSetting: (PrivacySettings) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = NullText) }
            Text("Privacy settings", color = NullText, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        SettingRow("Show online status", settings.showOnlineStatus) { onToggleSetting(settings.copy(showOnlineStatus = it)) }
        SettingRow("Read receipts", settings.readReceipts) { onToggleSetting(settings.copy(readReceipts = it)) }
        SettingRow("Message notifications", settings.messageNotifications) { onToggleSetting(settings.copy(messageNotifications = it)) }
        Spacer(Modifier.height(12.dp))
        Text("Connection: ${if (online) "online" else "offline"}", color = if (online) NullCyan else NullMuted)
    }
}

@Composable
private fun SettingRow(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = NullText)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun TextButtonish(text: String, onClick: () -> Unit) {
    Text(text, color = NullCyan, modifier = Modifier.clickable(onClick = onClick).padding(4.dp))
}

private fun formatTime(ms: Long): String {
    val minute = ((ms / 60000) % 60).toString().padStart(2, '0')
    val hour = ((ms / 3600000) % 24).toString().padStart(2, '0')
    return "$hour:$minute"
}

private fun statusLabel(status: MessageStatus): String = when (status) {
    MessageStatus.SENDING -> "sending"
    MessageStatus.DELIVERED -> "delivered"
    MessageStatus.READ -> "read"
}
