package com.privacyview.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.widget.RemoteViews
import com.privacyview.app.MainActivity
import com.privacyview.app.PrivacyApp
import com.privacyview.app.R
import com.privacyview.app.service.PrivacyOverlayService

class PrivacyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
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
                PrivacyOverlayService.start(context)
            } else {
                PrivacyOverlayService.stop(context)
            }

            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_PRIVACY = "com.privacyview.app.ACTION_TOGGLE_PRIVACY"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_privacy_layout)
            val prefs = PrivacyApp.instance.preferences
            val isEnabled = prefs.isPrivacyEnabledSync()

            val statusText = if (isEnabled) {
                context.getString(R.string.privacy_on)
            } else {
                context.getString(R.string.privacy_off)
            }

            views.setTextViewText(R.id.widget_status_text, statusText)

            if (isEnabled) {
                views.setTextColor(R.id.widget_status_text, Color.parseColor("#00E599"))
                views.setInt(R.id.widget_status_dot, "setColorFilter", Color.parseColor("#00E599"))
            } else {
                views.setTextColor(R.id.widget_status_text, Color.parseColor("#8E8E93"))
                views.setInt(R.id.widget_status_dot, "setColorFilter", Color.parseColor("#8E8E93"))
            }

            val toggleIntent = Intent(context, PrivacyWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PRIVACY
            }
            val pendingToggle = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingToggle)

            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingOpen = PendingIntent.getActivity(
                context,
                1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingOpen)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val thisWidget = ComponentName(context, PrivacyWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
