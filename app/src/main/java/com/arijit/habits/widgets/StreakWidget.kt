package com.arijit.habits.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.arijit.habits.MainActivity
import com.arijit.habits.R

class StreakWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_streak)

            val prefs = context.getSharedPreferences("ai_settings_prefs", Context.MODE_PRIVATE)
            val streakCount = prefs.getInt("current_streak", 0)

            if (streakCount == 1)
                views.setTextViewText(R.id.streak_txt_widget, "$streakCount day")
            else
                views.setTextViewText(R.id.streak_txt_widget, "$streakCount days")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}