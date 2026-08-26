package com.example.offlinenotes

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.repository.SecurityRepository
import com.example.offlinenotes.domain.repository.SettingsRepository
import com.example.offlinenotes.presentation.navigation.AppNavGraph
import com.example.offlinenotes.presentation.theme.OfflineNotesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
            val isBiometricEnabled by securityRepository.isBiometricEnabled.collectAsState(initial = false)
            val isScreenshotProtected by securityRepository.isScreenshotProtectionEnabled.collectAsState(initial = false)
            
            var isAuthenticated by remember { mutableStateOf(!isBiometricEnabled) }

            LaunchedEffect(isBiometricEnabled) {
                if (isBiometricEnabled && !isAuthenticated) {
                    showBiometricPrompt { authenticated ->
                        isAuthenticated = authenticated
                        if (!authenticated) finish()
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
                if (isAuthenticated) {
                    val startDestination = if (settings?.isOnboardingCompleted == true) "home" else "onboarding"
                    AppNavGraph(startDestination = startDestination)
                }
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

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Premium Notes Locked")
            .setSubtitle("Authenticate to access your private notes")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}