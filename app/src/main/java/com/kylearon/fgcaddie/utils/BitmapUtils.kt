package com.kylearon.fgcaddie.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import com.kylearon.fgcaddie.camera.DrawableCanvasView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class BitmapUtils {

    companion object {

        private const val TAG = "BitmapUtils"

        fun rotateBitmap(original: Bitmap, degrees: Float): Bitmap? {
            val width = original.width;
            val height = original.height;
            val matrix = Matrix();
            matrix.preRotate(degrees);
            val rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
            return rotatedBitmap;
        }

        /**
         * A function to write the bitmap to the private user files directory
         */
        fun saveBitmapToFileStorage(bitmap: Bitmap, name: String, context: Context) {
            //write the file out to local storage
            val file = File(context.filesDir, name);
            val fileOut = FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
            fileOut.close();
//        Log.d("LocalCourseApiImpl", "!!!! Wrote to: " + file.absolutePath);
        }

        /**
         * UNUSED FOR NOW
         *
         * A function to write the bitmap to the public media directories.
         */
        private fun saveBitmapAsFile(bitmap: Bitmap, name: String, context:Context) {
            val values = contentValues()!!;
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FGCaddie");
            values.put(MediaStore.Images.Media.IS_PENDING, true);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            context.contentResolver.update(uri!!, values, null, null);

            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri));
                values.put(MediaStore.Images.Media.IS_PENDING, false);
                context.contentResolver.update(uri, values, null, null);

            }
        }

        /**
         * Used by saveBitmapAsFile()
         */
        private fun contentValues(): ContentValues? {
            val values = ContentValues();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            return values;
        }

        /**
         * Used by saveBitmapAsFile()
         */
        private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
            Log.d(TAG,"saveImageToStream()");
            if (outputStream != null) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();

                    // success dialog
                    val msg = "!! saved photo";
//                Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, msg);

                } catch (e: Exception) {

                    e.printStackTrace();
                    val msg = "!! ERROR: did not save photo";
//                Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, msg);
                }
            }
        }

    }
}