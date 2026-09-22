package com.antiphonesnatcher.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.SizeF
import android.widget.RemoteViews
import com.antiphonesnatcher.app.MainActivity
import com.antiphonesnatcher.app.PrivacyApp
import com.antiphonesnatcher.app.R
import com.antiphonesnatcher.app.service.PrivacyOverlayService

open class PrivacyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_PRIVACY) {
            if (!Settings.canDrawOverlays(context)) {
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(appIntent)
                return
            }

            val prefs = PrivacyApp.instance.preferences
            val nextState = !prefs.isPrivacyEnabledSync()
            prefs.setPrivacyEnabled(nextState)

            if (nextState) {
                PrivacyOverlayService.triggerPanic(context)
            } else {
                PrivacyOverlayService.dismissPanic(context)
            }

            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_PRIVACY = "com.antiphonesnatcher.app.ACTION_TOGGLE_PRIVACY"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
            val prefs = PrivacyApp.instance.preferences
            val isEnabled = prefs.isPrivacyEnabledSync()

            val toggleIntent = Intent(context, PrivacyWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PRIVACY
            }
            val pendingToggle = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            fun populateViews(views: RemoteViews, layoutType: Int) {
                // layoutType: 0 = compact, 1 = medium, 2 = large
                val buttonText = when (layoutType) {
                    2 -> if (isEnabled) "Turn Off Privacy" else "Turn On Privacy"
                    else -> if (isEnabled) "OFF" else "ON"
                }
                views.setTextViewText(R.id.widget_action_button, buttonText)

                if (isEnabled) {
                    views.setInt(R.id.widget_action_button, "setBackgroundResource", R.drawable.widget_btn_on)
                    views.setTextColor(R.id.widget_action_button, Color.parseColor("#0A0A0E"))
                } else {
                    views.setInt(R.id.widget_action_button, "setBackgroundResource", R.drawable.widget_btn_off)
                    views.setTextColor(R.id.widget_action_button, Color.parseColor("#FFFFFF"))
                }

                if (layoutType >= 1) {
                    val subtitleText = if (isEnabled) "Active Protection" else "Inactive"
                    views.setTextViewText(R.id.widget_status_subtitle, subtitleText)
                    views.setTextColor(
                        R.id.widget_status_subtitle,
                        if (isEnabled) Color.parseColor("#00E599") else Color.parseColor("#8E8E93")
                    )
                    views.setOnClickPendingIntent(R.id.widget_app_title, pendingOpen)
                }

                if (layoutType == 2) {
                    val badgeText = if (isEnabled) "Active" else "Standby"
                    views.setTextViewText(R.id.widget_mode_badge, badgeText)
                    views.setTextColor(
                        R.id.widget_mode_badge,
                        if (isEnabled) Color.parseColor("#00E599") else Color.parseColor("#8E8E93")
                    )
                }

                views.setOnClickPendingIntent(R.id.widget_action_button, pendingToggle)
                views.setOnClickPendingIntent(R.id.widget_root, pendingToggle)
                views.setOnClickPendingIntent(R.id.widget_logo, pendingOpen)
            }

            val finalViews: RemoteViews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val compactViews = RemoteViews(context.packageName, R.layout.widget_privacy_layout_compact)
                val regularViews = RemoteViews(context.packageName, R.layout.widget_privacy_layout)
                val largeViews = RemoteViews(context.packageName, R.layout.widget_privacy_layout_large)

                populateViews(compactViews, 0)
                populateViews(regularViews, 1)
                populateViews(largeViews, 2)

                val viewMapping = mapOf(
                    SizeF(60f, 30f) to compactViews,
                    SizeF(170f, 40f) to regularViews,
                    SizeF(170f, 130f) to largeViews
                )
                RemoteViews(viewMapping)
            } else {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

                val layoutType = when {
                    minHeight >= 120 -> 2
                    minWidth >= 170 -> 1
                    else -> 0
                }

                val layoutId = when (layoutType) {
                    2 -> R.layout.widget_privacy_layout_large
                    1 -> R.layout.widget_privacy_layout
                    else -> R.layout.widget_privacy_layout_compact
                }

                val views = RemoteViews(context.packageName, layoutId)
                populateViews(views, layoutType)
                views
            }

            appWidgetManager.updateAppWidget(appWidgetId, finalViews)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val providers = listOf(
                ComponentName(context, PrivacyWidgetProvider::class.java),
                ComponentName(context, CompactWidgetProvider::class.java),
                ComponentName(context, LargeWidgetProvider::class.java)
            )
            for (provider in providers) {
                val ids = appWidgetManager.getAppWidgetIds(provider)
                for (id in ids) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
