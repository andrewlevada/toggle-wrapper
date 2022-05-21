package com.andrewlevada.togglewrapper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.andrewlevada.togglewrapper.service.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val STOP_TIMER_ACTION = "com.andrewlevada.togglewrapper.STOP_TIMER_ACTION";
const val REFRESH_ACTION = "com.andrewlevada.togglewrapper.REFRESH_ACTION";

class TimerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        initDatabase(context)

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
                initDatabase(context)
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
    var views = RemoteViews(context.packageName, R.layout.timer_widget)

    views.setOnClickPendingIntent(R.id.stop_time_button, getPendingSelfIntent(context, STOP_TIMER_ACTION, appWidgetId))
    views.setOnClickPendingIntent(R.id.widget_surface, getPendingSelfIntent(context, REFRESH_ACTION, appWidgetId))

    appWidgetManager.updateAppWidget(appWidgetId, views)
    runningTimeEntryUpdate(views, appWidgetManager, appWidgetId, null, null)

    getCurrentTimeEntry { timeEntry ->
        if (timeEntry == null) return@getCurrentTimeEntry // TODO: show different layout
        runningTimeEntryUpdate(views, appWidgetManager, appWidgetId, timeEntry, null)

        val job = tickTime {
            views.setTextViewText(R.id.time_entry_time, getDisplayDurationOfTimeEntry(timeEntry))
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }

        if (timeEntry.pid != null) {
            var project = db().toggleProjectsDao().getById(timeEntry.pid)
            if (project == null) syncProjectsInDao(timeEntry.wid) {
                project = db().toggleProjectsDao().getById(timeEntry.pid)
                runningTimeEntryUpdate(views, appWidgetManager, appWidgetId, timeEntry, project)
            } else runningTimeEntryUpdate(views, appWidgetManager, appWidgetId, timeEntry, project)
        }
    }
}

internal fun runningTimeEntryUpdate(
    views: RemoteViews,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    timeEntry: ToggleTimeEntry?,
    project: ToggleProject?,
) {
    if (timeEntry == null) {
        views.setTextViewText(R.id.time_entry_title, "Idle")
        views.setViewVisibility(R.id.time_entry_label, View.GONE)
        views.setViewVisibility(R.id.time_entry_time, View.GONE)
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        return
    }

    views.setTextViewText(R.id.time_entry_time, getDisplayDurationOfTimeEntry(timeEntry))
    views.setViewVisibility(R.id.time_entry_time, View.VISIBLE)

    val info = "${project?.name ?: ""} ${timeEntry.tags.toString()}"

    if (timeEntry.description != null) {
        views.setTextViewText(R.id.time_entry_title, timeEntry.description)
        views.setTextViewText(R.id.time_entry_label, info)
        views.setViewVisibility(R.id.time_entry_label, View.VISIBLE)
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        return
    }

    views.setTextViewText(R.id.time_entry_title, info)
    views.setViewVisibility(R.id.time_entry_label, View.GONE)
    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
}

internal fun tickTime(callback: () -> Unit): Job {
    val scope = MainScope()
    var debugLimit = 0
    return scope.launch {
        while (debugLimit < 20) {
            delay(1000)
            debugLimit++
            callback.invoke()
        }
    }
}

internal fun getPendingSelfIntent(context: Context, action: String, appWidgetId: Int): PendingIntent {
    val intent = Intent(context, TimerWidget::class.java)
    intent.action = action
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}