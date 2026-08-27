package com.example.offlinenotes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.repository.SecurityRepository
import com.example.offlinenotes.domain.repository.SettingsRepository
import com.example.offlinenotes.presentation.navigation.AppNavGraph
import com.example.offlinenotes.presentation.theme.OfflineNotesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var securityRepository: SecurityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkSecurity()

        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = null)
            val isBiometricEnabled by securityRepository.isBiometricEnabled.collectAsState(initial = null)
            val isScreenshotProtected by securityRepository.isScreenshotProtectionEnabled.collectAsState(initial = false)
            
            var isAuthenticated by remember { mutableStateOf(false) }
            var isAuthChecked by remember { mutableStateOf(false) }
            var showNoSecurityDialog by remember { mutableStateOf(false) }

            val isLoading = settings == null || isBiometricEnabled == null

            LaunchedEffect(isBiometricEnabled) {
                val enabled = isBiometricEnabled
                if (enabled != null) {
                    if (enabled && !isAuthenticated) {
                        val status = checkBiometricAvailability()
                        if (status == BiometricManager.BIOMETRIC_SUCCESS || status == BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
                            showBiometricPrompt { authenticated ->
                                if (authenticated) {
                                    isAuthenticated = true
                                    isAuthChecked = true
                                } else {
                                    finish()
                                }
                            }
                        } else if (status == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                            showNoSecurityDialog = true
                        } else {
                            isAuthenticated = true
                            isAuthChecked = true
                        }
                    } else {
                        isAuthenticated = true
                        isAuthChecked = true
                    }
                }
            }

            LaunchedEffect(isScreenshotProtected) {
                if (isScreenshotProtected) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            
            val darkTheme = when(settings?.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM, null -> isSystemInDarkTheme()
            }

            OfflineNotesTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading || !isAuthChecked) {
                        SplashScreen()
                    } else {
                        val startDestination = if (settings?.isOnboardingCompleted == true) "home" else "onboarding"
                        AppNavGraph(startDestination = startDestination)
                    }

                    if (showNoSecurityDialog) {
                        SecuritySetupDialog(
                            onDismiss = { 
                                showNoSecurityDialog = false
                                isAuthenticated = true
                                isAuthChecked = true
                            },
                            onOpenSettings = {
                                openSecuritySettings()
                                showNoSecurityDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SecuritySetupDialog(onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Setup Required") },
            text = { Text("Biometric Lock is enabled, but your device has no PIN or Fingerprint set up. Please secure your device to protect your notes.") },
            confirmButton = {
                Button(onClick = onOpenSettings) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Later") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    private fun openSecuritySettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, 
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        startActivity(intent)
    }

    private fun checkBiometricAvailability(): Int {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }

    @Composable
    private fun SplashScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Syncing your thoughts...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }

    private fun checkSecurity() {
        if (securityRepository.isDeviceRooted()) {
            Toast.makeText(this, "Security Warning: Device is rooted. Use with caution.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Premium Notes Locked")
            .setSubtitle("Use fingerprint or your device PIN to continue")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
