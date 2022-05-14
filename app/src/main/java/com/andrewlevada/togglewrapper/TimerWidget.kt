package com.andrewlevada.togglewrapper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.andrewlevada.togglewrapper.service.getCurrentTimeEntry

const val STOP_TIMER_ACTION = "com.andrewlevada.togglewrapper.STOP_TIMER_ACTION";

class TimerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        println("onUpdate")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (STOP_TIMER_ACTION == intent.action) {
            println("STOP_TIMER_ACTION")
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.timer_widget)

    println("HELOOO")

    getCurrentTimeEntry { timeEntry ->
        if (timeEntry == null) return@getCurrentTimeEntry

        views.setTextViewText(R.id.timeEntryLabel, timeEntry.description)
        views.setTextViewText(R.id.timeEntryTime, timeEntry.start)

        views.setOnClickPendingIntent(R.id.stopTimeButton, getPendingSelfIntent(context, STOP_TIMER_ACTION))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

}

internal fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
    val intent = Intent(context, TimerWidget::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(context, 0, intent, 0)
}