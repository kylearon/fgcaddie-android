package com.kylearon.fgcaddie.utils

import java.text.SimpleDateFormat
import java.util.*

class FileUtils {

    companion object {

        private const val TAG = "FileUtils"

        private const val ANDROID_BASE_FILEPATH = "/data/user/0/com.kylearon.fgcaddie/files/";
        private const val ANDROID_BASE_FILEPATH_URI = "file:///data/user/0/com.kylearon.fgcaddie/files/";

        const val SHOT_TYPE_ORIGINAL = "original";
        const val SHOT_TYPE_MARKEDUP = "markedup";

        fun constructImageFilename(shotGuid: String, type: String): String {
            val filename: String = "shot-" + type + "_" + shotGuid + ".png";
            return filename;
        }

        fun getTimestamp(): String {
            val timestamp: String = SimpleDateFormat("yyyyMMdd:HHmmss").format(Date());
            return timestamp;
        }

        fun getDatetimeReadable(): String {
            //get the datetime
            val sdf = SimpleDateFormat("dd/MMM/yyyy hh:mm:ss");
            val currentDate = sdf.format(Calendar.getInstance().time);
            return currentDate;
        }

        /**
         * Use when you need to parse the URI afterwards.
         */
        fun getPrivateAppStorageFilepathURI(filename: String): String {
            val filepath = ANDROID_BASE_FILEPATH_URI + filename;
            return filepath;
        }

        /**
         * Use when you need to load straight into a File() constructor
         */
        fun getPrivateAppStorageFilepath(filename: String): String {
            val filepath = ANDROID_BASE_FILEPATH + filename;
            return filepath;
        }

    }

}