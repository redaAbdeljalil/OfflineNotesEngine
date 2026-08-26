package com.example.offlinenotes.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            "Offline First",
            "Your thoughts belong to you. Notes are stored locally and sync automatically when you're online.",
            Icons.Outlined.WifiOff,
            MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            "Military-Grade Security",
            "Everything is encrypted at rest. Protect your private notes with Biometric Fingerprint or Face ID.",
            Icons.Outlined.Lock,
            Color(0xFF2D6A4F)
        ),
        OnboardingPage(
            "Time Machine",
            "Never lose a change. Browse your note's history and restore any version with a single tap.",
            Icons.Outlined.History,
            Color(0xFF0061A4)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == iteration) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next/Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { index ->
            val page = pages[index]
            OnboardingPageContent(page)
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = page.color
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
