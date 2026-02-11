package com.example.timelimit.feature.onboarding.permission

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.timelimit.core.service.TimeLimitService
import com.example.timelimit.core.ui.theme.*
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(navController: NavController) {
    val context = LocalContext.current
    var notificationRequestLaunched by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Not needed for older versions
        }
    }

    // This state is used to trigger a re-check when the app is resumed
    var trigger by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                trigger = !trigger
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // We remember the state of the usage permission, re-calculating when the app is resumed.
    val usageStatsOk = remember(trigger) { hasUsageStatsPermission() }
    val notificationOk = notificationPermissionState?.status?.isGranted ?: true
    val canDrawOverlaysOk = remember(trigger) { hasOverlayPermission() }

    LaunchedEffect(usageStatsOk, notificationOk, canDrawOverlaysOk) {
        if (usageStatsOk && notificationOk && canDrawOverlaysOk) {
            context.startService(Intent(context, TimeLimitService::class.java))
            navController.navigate("main") { popUpTo("permission") { inclusive = true } }
        }
    }

    PremiumBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Setup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.glow(
                                color = MaterialTheme.colorScheme.primary,
                                alpha = 0.1f,
                                blurRadius = 8.dp
                            )
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
        AnimatedContent(
            targetState = when {
                !usageStatsOk -> "usage"
                !notificationOk -> "notification"
                !canDrawOverlaysOk -> "overlay"
                else -> "done"
            },
            label = "permission_screen_content",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { targetState ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (targetState) {
                    "usage" -> {
                        PermissionStep(
                            icon = Icons.Default.Settings,
                            title = "Action Required",
                            text = """This app needs Usage Access to identify which app is running.
                            |
                            |Please tap the button below, find 'TimeLimit' in the list, and enable the permission.""".trimMargin(),
                            buttonText = "Grant Usage Access",
                            onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                        )
                    }
                    "notification" -> {
                        val postNotificationsPermission = notificationPermissionState?.status
                        val isPermanentlyDenied = postNotificationsPermission is PermissionStatus.Denied &&
                                !postNotificationsPermission.shouldShowRationale &&
                                notificationRequestLaunched

                        if (isPermanentlyDenied) {
                            PermissionStep(
                                icon = Icons.Default.Notifications,
                                title = "One More Step",
                                text = "You have permanently denied the notification permission. Please enable it in the app settings to continue.",
                                buttonText = "Open App Settings",
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = Uri.fromParts("package", context.packageName, null)
                                    context.startActivity(intent)
                                }
                            )
                        } else {
                            PermissionStep(
                                icon = Icons.Default.Notifications,
                                title = "One More Step",
                                text = "Excellent! Now, please grant the Notification permission so the app can function correctly.",
                                buttonText = "Grant Notification Permission",
                                onClick = {
                                    notificationPermissionState?.launchPermissionRequest()
                                    notificationRequestLaunched = true
                                }
                            )
                        }
                    }
                    "overlay" -> {
                        PermissionStep(
                            icon = Icons.Default.Settings,
                            title = "Final Permission",
                            text = """To effectively block apps, this app needs permission to display over other apps. This allows it to launch itself to the foreground.
                            |
                            |Please find TimeLimit in the list and enable the permission.""".trimMargin(),
                            buttonText = "Grant Overlay Permission",
                            onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                    "done" -> {
                        PermissionStep(
                            icon = Icons.Default.CheckCircle,
                            title = "Thank You!",
                            text = "All permissions granted. Taking you to the app...",
                            buttonText = null,
                            onClick = {}
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun PermissionStep(icon: ImageVector, title: String, text: String, buttonText: String?, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .glow(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.1f,
                borderRadius = 28.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            if (buttonText != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
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
                        buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "After granting permission, please return to the app to continue.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 40.dp)
    )
}
