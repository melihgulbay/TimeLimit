package com.example.timelimit.core.service.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.timelimit.core.ui.R

object NotificationUtil {

    private const val CHANNEL_ID = "TimeLimitChannel"
    private const val BLOCKED_APP_CHANNEL_ID = "BlockedAppChannel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Time Limit Service Channel"
            val descriptionText = "Channel for Time Limit background service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val blockedAppChannelName = "Blocked App Alerts"
            val blockedAppChannelDesc = "Notifications for when a blocked app is opened"
            val blockedAppImportance = NotificationManager.IMPORTANCE_HIGH
            val blockedAppChannel = NotificationChannel(BLOCKED_APP_CHANNEL_ID, blockedAppChannelName, blockedAppImportance).apply {
                description = blockedAppChannelDesc
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(blockedAppChannel)
        }
    }

    fun createNotification(context: Context, contentText: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Time Limit")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun showBlockedAppNotification(context: Context, appName: String) {
        val builder = NotificationCompat.Builder(context, BLOCKED_APP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("App Blocked")
            .setContentText("$appName is blocked.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(appName.hashCode(), builder.build())
    }
}
