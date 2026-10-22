package com.migraineguard.app.feature.settings

import androidx.room.InvalidationTracker
import com.migraineguard.app.core.data.db.AppDatabase
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.backup.BackupRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DriveBackupState(
    val backupBytes: Long? = null,
    val connected: Boolean = false,
    val busy: Boolean = false,
    val lastSavedMillis: Long? = null,
    val message: String = "Connect Drive to keep your account backed up.",
)

@OptIn(FlowPreview::class)
class DriveSyncRepository(
    private val account: String,
    private val prefs: UserPreferencesRepository,
    private val db: AppDatabase,
    private val backup: BackupRepository,
    private val scope: CoroutineScope,
    private val remote: DriveBackupTransport = DriveRestClient(),
) {
    private val mutableState = MutableStateFlow(DriveBackupState())
    val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private var token: String? = null
    private var refreshAccessToken: (suspend () -> String)? = null
    private var observer: Job? = null
    private var fingerprint: String? = null

    suspend fun connect(accessToken: String, email: String, refresh: (suspend () -> String)? = null) = mutex.withLock {
        require(AccountStorage.google(email) == account) { "Drive account does not match this profile." }
        mutableState.value = mutableState.value.copy(busy = true, message = "Checking your private Drive backup…")
        try {
            remote.verifyAccount(accessToken, email)
            val file = remote.download(accessToken)
            if (file != null) {
                val payload = CloudBackupFormat.decode(file.bytes, account)
                backup.mergeSnapshot(payload.journal, restoreProfile = false)
                prefs.restoreCloudSettings(payload.settings)
                fingerprint = CloudBackupFormat.fingerprint(payload)
            }
            token = accessToken
            refreshAccessToken = refresh
            prefs.setDriveSyncEnabled(true)
            mutableState.value = DriveBackupState(backupBytes = file?.bytes?.size?.toLong(), connected = true, busy = false, lastSavedMillis = null, message =
                if (file == null) "Drive connected. Your details will back up as you save them."
                else "Your account backup is restored. Checking this phone's health permissions next.")
        } catch (e: CancellationException) {
            token = null
            mutableState.value = DriveBackupState(message="Drive check paused. Your local data is preserved; reconnect when ready.")
            throw e
        } catch (e: Exception) {
            token = null
            mutableState.value = DriveBackupState(message = e.message ?: "Drive restore could not finish. Local data is preserved.")
            throw e
        }
        observer?.cancel()
        observer = scope.launch {
            combine(prefs.preferences.map { CloudBackupFormat.json.encodeToString(
                com.migraineguard.app.core.data.prefs.UserPreferences.serializer(),
                CloudBackupFormat.portable(it)) }.distinctUntilChanged(), journalChanges()) { _, _ -> Unit }
                .debounce(4_000).collect {
                    if (prefs.preferences.first().driveSyncEnabled) {
                        try { backupNow() } catch (e: CancellationException) { throw e
                        } catch (_: Exception) { /* A visible status remains; retry on the next change or manually. */ }
                    }
                }
        }
    }

    private fun journalChanges(): Flow<Unit> = kotlinx.coroutines.flow.callbackFlow {
        val observer = object : InvalidationTracker.Observer("migraine_episodes", "nutrition_scans",
            "water_history", "workouts", "weight_entries", "medications", "ai_sessions") {
            override fun onInvalidated(tables: Set<String>) { trySend(Unit) }
        }
        db.invalidationTracker.addObserver(observer)
        trySend(Unit)
        awaitClose { db.invalidationTracker.removeObserver(observer) }
    }

    suspend fun backupNow() = mutex.withLock {
        val previousToken = token ?: error("Reconnect Drive to save a backup.")
        if (!prefs.preferences.first().driveSyncEnabled) return@withLock
        mutableState.value = mutableState.value.copy(busy = true, message = "Saving your account to Drive…")
        try {
            val currentToken = refreshAccessToken?.invoke() ?: previousToken
            token = currentToken
            // Always inspect/merge the remote copy before writing; never overwrite a restore failure.
            val previous = remote.download(currentToken)
            if (previous != null) {
                val cloud = CloudBackupFormat.decode(previous.bytes, account)
                backup.mergeSnapshot(cloud.journal, restoreProfile = false)
                prefs.restoreCloudSettings(cloud.settings)
            }
            val settings = prefs.preferences.first()
            val payload = CloudAccountBackup(owner = account, savedAtMillis = System.currentTimeMillis(),
                settings = CloudBackupFormat.portable(settings), journal = backup.snapshot())
            val hash = CloudBackupFormat.fingerprint(payload)
            val encoded = CloudBackupFormat.encode(payload)
            val changed = hash != fingerprint
            if (changed) {
                remote.upload(currentToken, encoded, previous)
                fingerprint = hash
            }
            mutableState.value = DriveBackupState(backupBytes = (if (changed) encoded.size else (previous?.bytes?.size ?: encoded.size)).toLong(), connected = true, busy = false, lastSavedMillis = System.currentTimeMillis(), message =
                "Your profile, settings, API keys and journal are backed up to your private Drive app data.")
        } catch (e: CancellationException) {
            mutableState.value = mutableState.value.copy(busy=false,message="Backup paused. Your local data is preserved.")
            throw e
        } catch (e: Exception) {
            mutableState.value = mutableState.value.copy(busy = false,
                message = e.message ?: "Backup failed. Your local data is preserved.")
            throw e
        }
    }

    fun reconnectNeeded() {
        mutableState.value = mutableState.value.copy(busy = false, message = "Drive needs reconnecting. Your saved profile is available; open Account settings when online.")
    }

    fun dispose() { observer?.cancel(); observer = null; token = null; refreshAccessToken = null }

    suspend fun pause() {
        prefs.setDriveSyncEnabled(false)
        observer?.cancel(); observer = null; token = null; refreshAccessToken = null
        mutableState.value = DriveBackupState(message = "Automatic backup paused. Existing Drive files are retained.")
    }
}
