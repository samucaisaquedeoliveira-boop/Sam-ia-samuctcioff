package com.example.data.auth

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.dataStore by preferencesDataStore(name = "portal_sam_prefs")

object SecurityUtils {
    private const val ITERATIONS = 12000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verifyPassword(password: String, saltBase64: String, expectedHash: String): Boolean {
        val computed = hashPassword(password, saltBase64)
        return computed == expectedHash
    }
}

class UserPreferencesRepository(private val context: Context) {
    private val keyCurrentUid = stringPreferencesKey("current_uid")
    private val keyDarkTheme = booleanPreferencesKey("dark_theme")
    private val keyNeonEffects = booleanPreferencesKey("neon_effects")
    private val keyUpdateManifestUrl = stringPreferencesKey("update_manifest_url")

    val currentUserIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[keyCurrentUid]?.takeIf { it.isNotBlank() }
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyDarkTheme] ?: true
    }

    val neonEffectsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[keyNeonEffects] ?: true
    }

    val updateManifestUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyUpdateManifestUrl] ?: ""
    }

    suspend fun saveSessionUid(uid: String?) {
        context.dataStore.edit { prefs ->
            if (uid.isNullOrBlank()) prefs.remove(keyCurrentUid)
            else prefs[keyCurrentUid] = uid
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[keyDarkTheme] = enabled }
    }

    suspend fun setNeonEffects(enabled: Boolean) {
        context.dataStore.edit { it[keyNeonEffects] = enabled }
    }

    suspend fun setUpdateManifestUrl(url: String) {
        context.dataStore.edit { it[keyUpdateManifestUrl] = url.trim() }
    }
}

