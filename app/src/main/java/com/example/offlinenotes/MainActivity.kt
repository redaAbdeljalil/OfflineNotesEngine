package com.example.offlinenotes

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
class MainActivity : FragmentActivity() { // FragmentActivity for Biometrics

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var securityRepository: SecurityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkSecurity()

        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = null)
            val isBiometricEnabled by securityRepository.isBiometricEnabled.collectAsState(initial = null)
            val isScreenshotProtected by securityRepository.isScreenshotProtectionEnabled.collectAsState(initial = false)
            
            // App state management
            var isAuthenticated by remember { mutableStateOf(false) }
            var isAuthChecked by remember { mutableStateOf(false) }

            // Determine if we are still loading initial user preferences
            val isLoading = settings == null || isBiometricEnabled == null

            LaunchedEffect(isBiometricEnabled) {
                val enabled = isBiometricEnabled
                if (enabled != null) {
                    if (enabled) {
                        showBiometricPrompt { authenticated ->
                            isAuthenticated = authenticated
                            isAuthChecked = true
                            if (!authenticated) finish()
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
                    } else if (isAuthenticated) {
                        val startDestination = if (settings?.isOnboardingCompleted == true) "home" else "onboarding"
                        AppNavGraph(startDestination = startDestination)
                    }
                }
            }
        }
    }

    @Composable
    private fun SplashScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Premium Loader
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
                    // Only exit if it's a real failure, not just a cancelation of a previous prompt
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                         onResult(false)
                    } else {
                        onResult(false)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Premium Notes Locked")
            .setSubtitle("Authenticate to access your private notes")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}