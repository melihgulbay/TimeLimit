package com.example.timelimit.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.timelimit.core.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Outlined.WavingHand,
            title = "Welcome to TimeLimit",
            description = "Take control of your digital wellbeing and build healthier phone habits.",
            tip = "Join thousands who have improved their focus"
        ),
        OnboardingPage(
            icon = Icons.Outlined.Block,
            title = "Block Distracting Apps",
            description = "Choose which apps to limit during your focused hours. When you try to open them, TimeLimit will open instead.",
            tip = "Perfect for social media and games"
        ),
        OnboardingPage(
            icon = Icons.Outlined.Schedule,
            title = "Set Smart Schedules",
            description = "Create daily schedules for each day of the week. Block apps during work hours, study time, or before bed.",
            tip = "Different schedules for weekdays and weekends"
        ),
        OnboardingPage(
            icon = Icons.Outlined.BarChart,
            title = "Track Your Progress",
            description = "View detailed statistics about your app usage. See which apps consume most of your time.",
            tip = "Daily, weekly, and monthly insights"
        ),
        OnboardingPage(
            icon = Icons.Outlined.AutoAwesome,
            title = "Stay Focused",
            description = "Build better habits one day at a time. Reduce screen time and increase productivity.",
            tip = "Let's get started!"
        )
    )

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar with skip button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        IconButton(
                            onClick = {
                                if (currentPage > 0) {
                                    currentPage--
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    if (currentPage < pages.size - 1) {
                        TextButton(onClick = onComplete) {
                            Text(
                                "Skip",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                // Page content with swipe detection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(currentPage) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when {
                                    dragAmount > 50 && currentPage > 0 -> {
                                        currentPage--
                                    }
                                    dragAmount < -50 && currentPage < pages.size - 1 -> {
                                        currentPage++
                                    }
                                }
                            }
                        }
                ) {
                    Crossfade(
                        targetState = currentPage,
                        animationSpec = tween(durationMillis = 300),
                        label = "page_transition"
                    ) { page ->
                        OnboardingPageContent(pages[page])
                    }
                }

                // Page indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(
                                    width = if (isSelected) 24.dp else 8.dp,
                                    height = 8.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                                .animateContentSize()
                                .glow(
                                    color = MaterialTheme.colorScheme.primary,
                                    alpha = if (isSelected) 0.1f else 0f,
                                    borderRadius = 8.dp
                                )
                        )
                    }
                }

                // Bottom button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    if (currentPage < pages.size - 1) {
                        Button(
                            onClick = {
                                if (currentPage < pages.size - 1) {
                                    currentPage++
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 8.dp)
                                .glow(
                                    color = MaterialTheme.colorScheme.primary,
                                    alpha = 0.2f,
                                    borderRadius = 16.dp
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                "Next",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 8.dp)
                                .glow(
                                    color = MaterialTheme.colorScheme.primary,
                                    alpha = 0.3f,
                                    borderRadius = 16.dp
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                "Get Started",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji with animated scale
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(page) {
            visible = false
            kotlinx.coroutines.delay(100)
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .glow(
                        color = MaterialTheme.colorScheme.primary,
                        alpha = 0.15f,
                        borderRadius = 80.dp
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title with slide animation
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(400)
            ) + fadeIn()
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.glow(
                    color = MaterialTheme.colorScheme.primary,
                    alpha = 0.1f,
                    blurRadius = 8.dp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description with slide animation
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(400, delayMillis = 100)
            ) + fadeIn()
        ) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tip card
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(400, delayMillis = 200)
            ) + fadeIn()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .glow(
                        color = MaterialTheme.colorScheme.secondary,
                        alpha = 0.05f,
                        borderRadius = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = page.tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f),
                        lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                }
            }
        }
    }
}

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tip: String
)

