package com.kylearon.fgcaddie.data

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class SettingsRepository(
    private val settingsRemoteDataSource: SettingsRemoteDataSource
) {
    fun getSettings() : Settings? = settingsRemoteDataSource.getSettings();

    fun setSettings(settings: Settings) = settingsRemoteDataSource.setSettings(settings);
}