package com.example.offlinenotes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.presentation.MainViewModel
import com.example.offlinenotes.presentation.navigation.AppNavGraph
import com.example.offlinenotes.presentation.navigation.Screen
import com.example.offlinenotes.presentation.theme.OfflineNotesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (viewModel.isDeviceRooted()) {
            Toast.makeText(this, "Security Warning: Device is rooted. Use with caution.", Toast.LENGTH_LONG).show()
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
            var showNoSecurityDialog by remember { mutableStateOf(false) }

            LaunchedEffect(uiState.isBiometricEnabled) {
                if (uiState.isBiometricEnabled && !isAuthenticated) {
                    val status = checkBiometricAvailability()
                    if (status == BiometricManager.BIOMETRIC_SUCCESS || status == BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
                        showBiometricPrompt { authenticated ->
                            if (authenticated) {
                                viewModel.setAuthenticated(true)
                            } else {
                                finish()
                            }
                        }
                    } else if (status == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                        showNoSecurityDialog = true
                    } else {
                        viewModel.setAuthenticated(true)
                    }
                }
            }

            LaunchedEffect(uiState.isScreenshotProtected) {
                if (uiState.isScreenshotProtected) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            
            val darkTheme = when(uiState.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            OfflineNotesTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isLoading) {
                        SplashScreen()
                    } else {
                        if (isAuthenticated) {
                            val startDestination = if (uiState.isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route
                            AppNavGraph(startDestination = startDestination)
                        } else {
                            SplashScreen() 
                        }
                    }

                    if (showNoSecurityDialog) {
                        SecuritySetupDialog(
                            onDismiss = { 
                                showNoSecurityDialog = false
                                viewModel.setAuthenticated(true)
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
            title = { Text(stringResource(R.string.settings_section_security)) },
            text = { Text("Biometric Lock is enabled, but your device has no PIN or Fingerprint set up. Please secure your device to protect your notes.") },
            confirmButton = {
                Button(onClick = onOpenSettings) { Text(stringResource(R.string.common_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_later)) }
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
