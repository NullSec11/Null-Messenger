package com.nullsec.messenger.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AppRepository(val context: Context) {
    private val store = JsonStore(context)
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _users = MutableStateFlow(store.loadUsers())
    val users: StateFlow<List<AppUser>> = _users.asStateFlow()

    private val _threads = MutableStateFlow(store.loadThreads())
    val threads: StateFlow<List<ChatThread>> = _threads.asStateFlow()

    private val _messages = MutableStateFlow(store.loadMessages())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _pending = MutableStateFlow(store.loadPending())
    val pending: StateFlow<List<PendingOutgoing>> = _pending.asStateFlow()

    private val _settings = MutableStateFlow(store.loadSettings())
    val settings: StateFlow<PrivacySettings> = _settings.asStateFlow()

    private val _auth = MutableStateFlow(AuthState(currentUser = null, sessionToken = null))
    val auth: StateFlow<AuthState> = _auth.asStateFlow()

    private val _connectionOnline = MutableStateFlow(true)
    val connectionOnline: StateFlow<Boolean> = _connectionOnline.asStateFlow()

    init {
        store.loadSession()?.let { username ->
            _auth.value = _auth.value.copy(currentUser = _users.value.firstOrNull { it.username == username })
        }
        ensurePersonalThread()
    }

    fun setConnectionOnline(online: Boolean) {
        scope.launch {
            _connectionOnline.value = online
            if (online) flushQueue()
        }
    }

    fun login(username: String, password: CharArray): Result<Unit> = runCatching {
        val user = _users.value.firstOrNull { it.username.equals(username, ignoreCase = true) }
            ?: error("Пользователь не найден")
        if (!PasswordHasher.verify(password, user.passwordSaltB64, user.passwordHashB64)) error("Неверный пароль")
        _auth.value = AuthState(currentUser = user, sessionToken = UUID.randomUUID().toString())
        store.saveSession(user.username)
        updateUserOnline(user.username, true)
    }

    fun register(username: String, password: CharArray): Result<Unit> = runCatching {
        val cleaned = username.trim().lowercase()
        require(cleaned.length in 3..20) { "Имя должно быть 3–20 символов" }
        require(cleaned.all { it.isLetterOrDigit() || it == '_' }) { "Только латиница, цифры и _" }
        if (_users.value.any { it.username.equals(cleaned, ignoreCase = true) }) error("Имя уже занято")
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        val user = AppUser(cleaned, PasswordHasher.toB64(salt), PasswordHasher.toB64(hash))
        _users.value = _users.value + user
        store.saveUsers(_users.value)
        _auth.value = AuthState(currentUser = user, sessionToken = UUID.randomUUID().toString())
        store.saveSession(user.username)
        updateUserOnline(user.username, true)
    }

    fun logout() {
        _auth.value.currentUser?.let { updateUserOnline(it.username, false) }
        _auth.value = AuthState()
        store.saveSession(null)
    }

    fun deleteAccount() {
        val user = _auth.value.currentUser ?: return
        val username = user.username
        _threads.value = _threads.value.filterNot { it.peerUsername == username }
        _messages.value = _messages.value.filterNot { it.senderUsername == username }
        _pending.value = _pending.value.filterNot { it.threadId.startsWith(username) }
        _users.value = _users.value.filterNot { it.username == username }
        logout()
        store.clearAll()
        _threads.value = emptyList()
        _messages.value = emptyList()
        _pending.value = emptyList()
    }

    fun searchUsers(query: String): List<AppUser> {
        val q = query.trim().lowercase()
        return _users.value.filter { it.username.contains(q) && (_auth.value.currentUser?.username != it.username) }
    }

    fun createThread(peerUsername: String): ChatThread {
        val me = requireNotNull(_auth.value.currentUser) { "Нужен вход" }
        val existing = _threads.value.firstOrNull { it.peerUsername == peerUsername }
        if (existing != null) return existing
        val thread = ChatThread(id = UUID.randomUUID().toString(), peerUsername = peerUsername)
        _threads.value = listOf(thread) + _threads.value
        store.saveThreads(_threads.value)
        ensurePersonalThread(me.username, peerUsername)
        return thread
    }

    fun getThread(threadId: String): ChatThread? = _threads.value.firstOrNull { it.id == threadId }
    fun getMessages(threadId: String): List<ChatMessage> = _messages.value.filter { it.threadId == threadId }.sortedBy { it.createdAt }

    fun sendMessage(threadId: String, text: String) {
        val me = _auth.value.currentUser ?: return
        val clean = text.trim()
        if (clean.isBlank()) return
        val encrypted = MessageCrypto.encrypt(clean)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            threadId = threadId,
            senderUsername = me.username,
            encryptedBody = encrypted,
            status = if (_connectionOnline.value) MessageStatus.DELIVERED else MessageStatus.SENDING,
        )
        _messages.value = _messages.value + msg
        updateThreadPreview(threadId, clean, msg.createdAt)
        if (_connectionOnline.value) {
            markDelivered(msg.id)
            scheduleAutoReply(threadId)
        } else {
            _pending.value = _pending.value + PendingOutgoing(msg.id, threadId, clean, msg.createdAt)
            store.savePending(_pending.value)
        }
        store.saveMessages(_messages.value)
        store.saveThreads(_threads.value)
    }

    fun flushQueue() {
        if (!_connectionOnline.value) return
        val queued = _pending.value
        if (queued.isEmpty()) return
        queued.forEach { item ->
            val message = _messages.value.firstOrNull { it.id == item.messageId } ?: return@forEach
            markDelivered(message.id)
            scheduleAutoReply(item.threadId)
        }
        _pending.value = emptyList()
        store.savePending(_pending.value)
        store.saveMessages(_messages.value)
    }

    fun markThreadRead(threadId: String) {
        val me = _auth.value.currentUser?.username ?: return
        val changed = _messages.value.map { msg ->
            if (msg.threadId == threadId && msg.senderUsername != me && msg.readAt == null) {
                msg.copy(status = MessageStatus.READ, readAt = System.currentTimeMillis())
            } else msg
        }
        _messages.value = changed
        _threads.value = _threads.value.map { if (it.id == threadId) it.copy(unreadCount = 0) else it }
        store.saveMessages(_messages.value)
        store.saveThreads(_threads.value)
    }

    fun deleteMessageLocally(messageId: String) {
        _messages.value = _messages.value.map { if (it.id == messageId) it.copy(isDeletedLocally = true) else it }
        store.saveMessages(_messages.value)
    }

    fun updateSettings(settings: PrivacySettings) {
        _settings.value = settings
        store.saveSettings(settings)
    }

    fun togglePresence(isOnline: Boolean) {
        _auth.value.currentUser?.let { updateUserOnline(it.username, isOnline) }
    }

    private fun ensurePersonalThread(me: String = _auth.value.currentUser?.username.orEmpty(), peer: String = "nova") {
        if (me.isNotBlank() && _threads.value.none { it.peerUsername == peer }) {
            _threads.value = listOf(ChatThread(id = UUID.randomUUID().toString(), peerUsername = peer)) + _threads.value
            store.saveThreads(_threads.value)
        }
    }

    private fun ensurePersonalThread() {
        if (_threads.value.isEmpty()) ensurePersonalThread(peer = "nova")
    }

    private fun updateThreadPreview(threadId: String, text: String, at: Long) {
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(lastMessagePreview = text.take(28), lastMessageAt = at, unreadCount = if (_auth.value.currentUser?.username == null) it.unreadCount else it.unreadCount)
            else it
        }.sortedByDescending { it.lastMessageAt }
    }

    private fun markDelivered(messageId: String) {
        val now = System.currentTimeMillis()
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(status = MessageStatus.DELIVERED, deliveredAt = now) else it
        }
    }

    private fun scheduleAutoReply(threadId: String) {
        val me = _auth.value.currentUser?.username ?: return
        val peer = _threads.value.firstOrNull { it.id == threadId }?.peerUsername ?: return
        scope.launch {
            delay(1200)
            val reply = when ((0..3).random()) {
                0 -> "понял 🤝"
                1 -> "сейчас гляну"
                2 -> "окей, принял"
                else -> "жёстко, без базара 🔥"
            }
            val encrypted = MessageCrypto.encrypt(reply)
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                senderUsername = peer,
                encryptedBody = encrypted,
                status = MessageStatus.READ,
                deliveredAt = System.currentTimeMillis(),
                readAt = if (_settings.value.readReceipts) System.currentTimeMillis() else null,
            )
            _messages.value = _messages.value + message
            _threads.value = _threads.value.map {
                if (it.id == threadId) it.copy(lastMessagePreview = reply.take(28), lastMessageAt = message.createdAt, unreadCount = it.unreadCount + 1) else it
            }.sortedByDescending { it.lastMessageAt }
            store.saveMessages(_messages.value)
            store.saveThreads(_threads.value)
            if (_settings.value.messageNotifications) {
                NotificationHelper.showMessageNotification(context, peer, reply)
            }
        }
    }

    private fun updateUserOnline(username: String, isOnline: Boolean) {
        _users.value = _users.value.map { if (it.username == username) it.copy(isOnline = isOnline) else it }
        store.saveUsers(_users.value)
    }
}
