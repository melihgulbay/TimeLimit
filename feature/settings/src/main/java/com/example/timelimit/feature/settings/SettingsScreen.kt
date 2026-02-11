package com.example.timelimit.feature.settings

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timelimit.core.model.UiState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timelimit.core.model.AppInfo
import com.example.timelimit.core.model.DaySchedule
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import com.example.timelimit.core.ui.theme.*
import androidx.compose.material.icons.outlined.*

enum class SortOption {
    NAME, BLOCKED_FIRST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showRemoveDialog by remember { mutableStateOf(false) }
    var appToRemove by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sort options state
    var sortOption by remember { mutableStateOf(SortOption.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    PremiumBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.glow(
                                color = MaterialTheme.colorScheme.primary,
                                alpha = 0.1f,
                                blurRadius = 8.dp
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Sort apps",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("By Name") },
                                onClick = {
                                    sortOption = SortOption.NAME
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Blocked First") },
                                onClick = {
                                    sortOption = SortOption.BLOCKED_FIRST
                                    showSortMenu = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showAppPicker() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.glow(
                        color = MaterialTheme.colorScheme.primary,
                        alpha = 0.3f,
                        borderRadius = 16.dp
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add app")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add App")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is UiState.Success -> {
                    val content = state.data
                    // Sort apps based on selected option
                    val sortedApps = remember(content.installedApps, content.blockedApps, sortOption) {
                        when (sortOption) {
                            SortOption.NAME -> content.installedApps.sortedBy { it.appName }
                            SortOption.BLOCKED_FIRST -> content.installedApps.sortedByDescending {
                                content.blockedApps.contains(it.packageName)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Info Card explaining the feature
                        item {
                            InfoCard(
                                title = "How it works",
                                description = "When you open a blocked app during scheduled hours, TimeLimit will automatically open instead, helping you stay focused."
                            )
                        }

                        // Schedule Section
                        if (content.schedules.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Daily Schedule",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(content.schedules) { schedule ->
                                DayScheduleItem(
                                    schedule = schedule,
                                    onScheduleChange = { viewModel.updateSchedule(it) }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }

                        // Blocked Apps Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Blocked Apps (${content.blockedApps.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (content.blockedApps.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Tap any app switch to unblock",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Info",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Help")
                                    }
                                }
                            }
                        }

                        if (sortedApps.isEmpty()) {
                            item {
                                EnhancedEmptyStateCard(
                                    onAddClick = { viewModel.showAppPicker() }
                                )
                            }
                        } else {
                            items(sortedApps) { appInfo ->
                                AppBlockCard(
                                    appInfo = appInfo,
                                    isBlocked = content.blockedApps.contains(appInfo.packageName),
                                    onToggle = { isBlocked ->
                                        if (isBlocked) {
                                            viewModel.toggleBlockedApp(appInfo.packageName)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "${appInfo.appName} is now blocked",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            // Show confirmation dialog for unblocking
                                            appToRemove = appInfo.packageName
                                            showRemoveDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // App Picker Dialog
                    if (content.showAppPicker) {
                        AppPickerDialog(
                            apps = content.installedApps,
                            blockedApps = content.blockedApps,
                            onDismiss = { viewModel.hideAppPicker() },
                            onAppToggle = { packageName ->
                                viewModel.toggleBlockedApp(packageName)
                                val app = content.installedApps.find { it.packageName == packageName }
                                if (app != null) {
                                    scope.launch {
                                        val isNowBlocked = content.blockedApps.contains(packageName)
                                        snackbarHostState.showSnackbar(
                                            message = if (isNowBlocked) "${app.appName} removed" else "${app.appName} added",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // Confirmation dialog for removing blocked app
                    if (showRemoveDialog && appToRemove != null) {
                        val appName = content.installedApps.find { it.packageName == appToRemove }?.appName ?: "this app"
                        AlertDialog(
                            onDismissRequest = {
                                showRemoveDialog = false
                                appToRemove = null
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = { Text("Unblock App?") },
                            text = {
                                Text("Are you sure you want to unblock $appName? It will no longer be restricted during scheduled hours.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        appToRemove?.let { pkg ->
                                            viewModel.toggleBlockedApp(pkg)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "$appName unblocked",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                        showRemoveDialog = false
                                        appToRemove = null
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Unblock")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showRemoveDialog = false
                                    appToRemove = null
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
                is UiState.Error -> {
                    // Error state UI
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { /* Could add retry here if needed */ }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerDialog(
    apps: List<AppInfo>,
    blockedApps: Set<String>,
    onDismiss: () -> Unit,
    onAppToggle: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Apps to Block")
                    Text(
                        text = "${blockedApps.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        },
        text = {
            if (filteredApps.isEmpty()) {
                // No results state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No apps found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try a different search term",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps) { app ->
                        val isChecked = blockedApps.contains(app.packageName)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAppToggle(app.packageName) },
                            color = if (isChecked)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        painter = rememberDrawablePainter(drawable = app.icon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            maxLines = 1
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onAppToggle(app.packageName) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InfoCard(title: String, description: String) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .glow(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.05f,
                borderRadius = 24.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EnhancedEmptyStateCard(onAddClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .glow(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.1f,
                borderRadius = 24.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No apps yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Start building better habits by adding apps you want to limit",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.glow(
                    color = MaterialTheme.colorScheme.primary,
                    alpha = 0.2f,
                    borderRadius = 12.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your First App")
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No apps yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add apps to your blocklist",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AppBlockCard(appInfo: AppInfo, isBlocked: Boolean, onToggle: (Boolean) -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .glow(
                color = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                alpha = 0.05f,
                borderRadius = 24.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = rememberDrawablePainter(drawable = appInfo.icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                    // Blocked indicator badge
                    if (isBlocked) {
                        Surface(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .shadow(4.dp, CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
                Column {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isBlocked) {
                        Text(
                            text = "Currently blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Switch(
                checked = isBlocked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.error,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun DayScheduleItem(schedule: DaySchedule, onScheduleChange: (DaySchedule) -> Unit) {
    val context = LocalContext.current
    val dayName = DateFormatSymbols().weekdays[schedule.dayOfWeek]

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val newTimeRange = schedule.timeRange.copy(startHour = hour, startMinute = minute)
            onScheduleChange(schedule.copy(timeRange = newTimeRange))
        },
        schedule.timeRange.startHour,
        schedule.timeRange.startMinute,
        false
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val newTimeRange = schedule.timeRange.copy(endHour = hour, endMinute = minute)
            onScheduleChange(schedule.copy(timeRange = newTimeRange))
        },
        schedule.timeRange.endHour,
        schedule.timeRange.endMinute,
        false
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .glow(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.05f,
                borderRadius = 24.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = { onScheduleChange(schedule.copy(isEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            AnimatedVisibility(
                visible = schedule.isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeButton(
                        label = "Start",
                        hour = schedule.timeRange.startHour,
                        minute = schedule.timeRange.startMinute,
                        onClick = { startTimePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    )
                    TimeButton(
                        label = "End",
                        hour = schedule.timeRange.endHour,
                        minute = schedule.timeRange.endMinute,
                        onClick = { endTimePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeButton(
    label: String,
    hour: Int,
    minute: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%02d:%02d", hour, minute),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
