package com.andrewlevada.togglewrapper.service

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.gsonDeserializer

data class ToggleResponse<T> (
    val data: T
)

data class ToggleTimeEntry (
    val id: Number,
    val start: String,
    val duration: Number,
    val description: String,
)

fun getCurrentTimeEntry(callback: (ToggleTimeEntry?) -> Unit) {
    Fuel.get("https://api.track.toggl.com/api/v8/time_entries/current")
        .authentication().basic(TOGGLE_API_KEY, "api_token")
        .responseObject<ToggleResponse<ToggleTimeEntry>>(gsonDeserializer()) { _, _, result ->
            callback.invoke(result.get().data)
        }
}