package com.example.intellipaatassignment.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/** Persists the access token, encrypted with a Keystore-backed key. */
class SessionStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val token: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            // A token that can't be decrypted (e.g. restored onto a new device whose Keystore
            // doesn't have the key) is treated as "logged out" rather than crashing.
            prefs[KEY_TOKEN]?.let { runCatching { cipher.decrypt(it) }.getOrNull() }
        }
        .flowOn(ioDispatcher)

    suspend fun saveToken(token: String) {
        val encrypted = withContext(ioDispatcher) { cipher.encrypt(token) }
        dataStore.edit { it[KEY_TOKEN] = encrypted }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY_TOKEN) }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("access_token")
    }
}
