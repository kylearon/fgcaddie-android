package com.kylearon.fgcaddie.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class LocalSettingsApiImpl(context: Context) : SettingsApi{

    val context = context;

    //start with an empty settings
    var _settings = Settings("default_creator");

    init {

        //init the settings from a saved file
        loadSettings();
    }


    override fun getSettings(): Settings? {
        return _settings;
    }

    override fun setSettings(settings: Settings) {
        _settings = settings;
        saveSettings();
    }

    private fun loadSettings() {

        //read the courses file from local storage
        var file = File(context.filesDir, "settings.json");

        //create and re-lookup the file if it doesn't exist
        if(!file.exists()) {
            saveSettings();
            file = File(context.filesDir, "settings.json");
        }

        val fileReader = FileReader(file);
        val bufferedReader = BufferedReader(fileReader);

        val loadedText = bufferedReader.readText();
//        Log.d("LocalSettingsApiImpl", loadedText);

        _settings = Json.decodeFromString(loadedText);
    }

    private fun saveSettings() {
        val jsonString = Json.encodeToString(_settings);
        Log.d("LocalSettingsApiImpl", jsonString);

        //write the file out to local storage
        val file = File(context.filesDir, "settings.json");
        val fileWriter = FileWriter(file);
        val bufferedWriter = BufferedWriter(fileWriter);
        bufferedWriter.write(jsonString);
        bufferedWriter.close();

        Log.d("LocalSettingsApiImpl", "Wrote to: " + file.absolutePath);
    }

}