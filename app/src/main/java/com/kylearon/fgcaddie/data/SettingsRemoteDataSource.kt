package com.kylearon.fgcaddie.data


/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class SettingsRemoteDataSource(
    private val settingsApi: SettingsApi
) {
    fun getSettings() = settingsApi.getSettings()

    fun setSettings(settings: Settings) = settingsApi.setSettings(settings)
}

// Makes settings-related network synchronous requests.
interface SettingsApi {
    fun getSettings(): Settings?

    fun setSettings(settings: Settings)
}
