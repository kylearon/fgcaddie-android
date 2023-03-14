package com.kylearon.fgcaddie.camera

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.kylearon.fgcaddie.R
import kotlinx.coroutines.*
import java.io.OutputStream
import java.util.*


private const val STROKE_WIDTH = 12f; // has to be float

//https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas
class DrawableCanvasView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context,  attrs) { //private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default


    private lateinit var extraCanvas: Canvas;
    lateinit var extraBitmap: Bitmap;

    private lateinit var backgroundBitmap: Bitmap;

    private var motionTouchEventX = 0f;
    private var motionTouchEventY = 0f;

    private var currentX = 0f;
    private var currentY = 0f;

    private var currentWidth = 0;
    private var currentHeight = 0;

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null);

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null);

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop;

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true // Smooths out edges of what is drawn without affecting shape.
        isDither = true // Dithering affects how colors with higher-precision than the device are down-sampled.
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private var path = Path();


    fun toBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    fun setBackgroundImage(background: Bitmap)
    {
        backgroundBitmap = Bitmap.createBitmap(background);
        extraBitmap = Bitmap.createBitmap(background);
        initScaledCanvas();
    }

    //call this after a new backgroundImage bitmap is given to this ImageView to init the Canvas and scale it correctly
    private fun initScaledCanvas()
    {
        extraCanvas = Canvas(extraBitmap);
        Log.d(TAG, "extraBitmap size: " + extraCanvas.width + " " + extraCanvas.height);

        val scaleWidth = extraBitmap.width.toFloat() / currentWidth.toFloat();
        val scaleHeight = extraBitmap.height.toFloat() / currentHeight.toFloat();

        extraCanvas.scale(scaleWidth, scaleHeight);
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        Log.d(TAG, "DCV size: " + width + " " + height);

        currentWidth = width;
        currentHeight = height;

        if (::extraBitmap.isInitialized) {
            extraBitmap.recycle();
        }

        if(backgroundBitmap != null) {
            extraBitmap = Bitmap.createBitmap(backgroundBitmap);
            Log.d(TAG, "extraBitmap size: " + extraBitmap.width + " " + extraBitmap.height);
        } else {
            extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        initScaledCanvas();

    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas);
        this.setImageBitmap(extraBitmap);
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x;
        motionTouchEventY = event.y;

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart();
            MotionEvent.ACTION_MOVE -> touchMove();
            MotionEvent.ACTION_UP -> touchUp();
        }
        return true;
    }

    private fun touchStart() {
        path.reset();
        path.moveTo(motionTouchEventX, motionTouchEventY);
        currentX = motionTouchEventX;
        currentY = motionTouchEventY;
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX);
        val dy = Math.abs(motionTouchEventY - currentY);
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2);
            currentX = motionTouchEventX;
            currentY = motionTouchEventY;

            Log.d(TAG, "touchMove() " + currentX + " " + currentY);

            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint);
        }
        invalidate();
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset();
    }

    fun saveBitmap(filename: String) {
            Log.d(TAG,"saveBitmap() " + filename);
            saveBitmapAsFile(extraBitmap, filename);
    }

    private fun saveBitmapAsFile(bitmap: Bitmap, name: String) {

        val values = contentValues()!!;
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FGCaddie");
        values.put(MediaStore.Images.Media.IS_PENDING, true);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri));
            values.put(MediaStore.Images.Media.IS_PENDING, false);
            context.contentResolver.update(uri, values, null, null);

//            var cursor: Cursor? = null
//            var uriFixed: String = "default";
//            try {
//                val proj = arrayOf(MediaStore.Images.Media.DATA);
//                cursor = context.contentResolver.query(uri, proj, null, null, null);
//                val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                cursor.moveToFirst();
//                uriFixed = cursor.getString(column_index);
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//
//            Log.d(TAG,"!!! URI: " + uri.path.toString());
//            Log.d(TAG,"!!! URI fixed: " + uriFixed);
        }
    }


    private fun contentValues(): ContentValues? {

        val values = ContentValues();

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        return values;

    }

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

                // warning dialog
                val msg = "!! ERROR: did not save photo";
//                Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, msg);

            }

        }

    }

    companion object {
        private const val TAG = "DrawableCanvasView"
    }
}