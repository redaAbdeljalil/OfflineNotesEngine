package com.example.offlinenotes.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)
    
    val isScreenshotProtectionEnabled: Flow<Boolean>
    suspend fun setScreenshotProtectionEnabled(enabled: Boolean)
    
    fun isDeviceRooted(): Boolean
    
    // In a real app, this would be managed via Keystore
    fun getDatabasePassphrase(): ByteArray
}
