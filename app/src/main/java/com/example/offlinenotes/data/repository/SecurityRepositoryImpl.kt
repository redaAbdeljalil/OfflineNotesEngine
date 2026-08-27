package com.example.offlinenotes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.offlinenotes.domain.repository.SecurityRepository
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

class SecurityRepositoryImpl @Inject constructor(
    private val context: Context
) : SecurityRepository {

    private object Keys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
    }

    override val isBiometricEnabled: Flow<Boolean> = context.securityDataStore.data
        .map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    override val isScreenshotProtectionEnabled: Flow<Boolean> = context.securityDataStore.data
        .map { it[Keys.SCREENSHOT_PROTECTION] ?: false }

    override suspend fun setScreenshotProtectionEnabled(enabled: Boolean) {
        context.securityDataStore.edit { it[Keys.SCREENSHOT_PROTECTION] = enabled }
    }

    override fun isDeviceRooted(): Boolean {
        return RootBeer(context).isRooted
    }

    override fun getDatabasePassphrase(): ByteArray {
        return "premium_security_key_2026".toByteArray()
    }
}
