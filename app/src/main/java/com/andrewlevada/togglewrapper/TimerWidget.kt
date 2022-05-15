package com.andrewlevada.togglewrapper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.andrewlevada.togglewrapper.service.getCurrentTimeEntry

const val STOP_TIMER_ACTION = "com.andrewlevada.togglewrapper.STOP_TIMER_ACTION";
const val REFRESH_ACTION = "com.andrewlevada.togglewrapper.REFRESH_ACTION";

class TimerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            STOP_TIMER_ACTION -> {
                println("STOP_TIMER_ACTION")
            }
            REFRESH_ACTION -> {
                // Get widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    var views = RemoteViews(context.packageName, R.layout.timer_widget)

    views.setOnClickPendingIntent(R.id.stop_time_button, getPendingSelfIntent(context, STOP_TIMER_ACTION, appWidgetId))
    views.setOnClickPendingIntent(R.id.widget_surface, getPendingSelfIntent(context, REFRESH_ACTION, appWidgetId))

    getCurrentTimeEntry { timeEntry ->
//        views = RemoteViews(context.packageName, R.layout.timer_widget)
        views.setTextViewText(R.id.timeEntryLabel, timeEntry?.description ?: "Idle")
        views.setTextViewText(R.id.timeEntryTime, timeEntry?.start ?: "")
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun getPendingSelfIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
    val intent = Intent(context, TimerWidget::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}