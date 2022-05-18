package com.andrewlevada.togglewrapper.service

import androidx.room.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.gsonDeserializer

data class ToggleResponse<T> (
    val data: T
)

data class ToggleTimeEntry (
    val id: Number,
    val wid: Number,
    val pid: Int?,
    val start: String,
    val duration: Number,
    val description: String?,
    val tags: List<String>?,
)

@Entity(tableName = "toggle_projects")
data class ToggleProject (
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "active") val active: Boolean,
)

fun getCurrentTimeEntry(callback: (ToggleTimeEntry?) -> Unit) {
    Fuel.get("https://api.track.toggl.com/api/v8/time_entries/current")
        .authentication().basic(TOGGLE_API_KEY, "api_token")
        .responseObject<ToggleResponse<ToggleTimeEntry>>(gsonDeserializer()) { _, _, result ->
            callback.invoke(result.get().data)
        }
}

fun syncProjectsInDao(workspaceId: Number, callback: () -> Unit) {
    Fuel.get("https://api.track.toggl.com/api/v8/workspaces/$workspaceId/projects")
        .authentication().basic(TOGGLE_API_KEY, "api_token")
        .responseObject<List<ToggleProject>>(gsonDeserializer()) { _, _, result ->
            db().toggleProjectsDao().reset()
            db().toggleProjectsDao().insert(result.get())
            callback.invoke()
        }
}