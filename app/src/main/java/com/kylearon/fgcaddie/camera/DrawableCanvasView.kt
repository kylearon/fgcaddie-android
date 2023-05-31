package com.kylearon.fgcaddie.camera

import android.content.ContentValues
import android.content.Context
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
import com.kylearon.fgcaddie.utils.BitmapUtils.Companion.saveBitmapToFileStorage
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


//https://developer.android.com/codelabs/advanced-android-kotlin-training-canvas
class DrawableCanvasView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context,  attrs) { //private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default


    private lateinit var extraCanvas: Canvas;
    lateinit var extraBitmap: Bitmap;

    private lateinit var backgroundBitmap: Bitmap;

    private val STROKE_WIDTH = 8f; // has to be float

    private var motionTouchEventX = 0f;
    private var motionTouchEventY = 0f;

    private var currentX = 0f;
    private var currentY = 0f;

    private var currentWidthDCV = 0;
    private var currentHeightDCV = 0;

    private var scaleWidthRatio = 0f;
    private var scaleHeightRatio = 0f;

    var offsetX = 0f;
    var offsetY = 0f;

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null);

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop;

    private val undoStack: LimitedSizeStack<Bitmap> = LimitedSizeStack(20);
    private val redoStack: LimitedSizeStack<Bitmap> = LimitedSizeStack(20);

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true // Smooths out edges of what is drawn without affecting shape.
        isDither = true // Dithering affects how colors with higher-precision than the device are down-sampled.
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH
    }

    private var path = Path();

    fun setPencilColor(pencilColor: Int) {
        paint.apply {
            color = pencilColor
        }
    }

    fun setPencilThickness(pencilThickness: Float) {
        paint.apply {
            strokeWidth = pencilThickness
        }
    }

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
        //init a Canvas which will be used to draw the path into
        extraCanvas = Canvas(extraBitmap);

        //calculate how much the ratio is to scale up the width or height to fit the DCV
        scaleWidthRatio = extraBitmap.width.toFloat() / currentWidthDCV.toFloat();
        scaleHeightRatio = extraBitmap.height.toFloat() / currentHeightDCV.toFloat();

        //whichever is the higher ratio is the axis which will be scaled to the full screen,
        //so scale the canvas by this value on both axes and calculate the offset for the other axis
        if(scaleWidthRatio > scaleHeightRatio) {
            extraCanvas.scale(scaleWidthRatio, scaleWidthRatio);

            val bitmapHeightInDCV = extraBitmap.height.toFloat() / scaleWidthRatio;
            offsetY = (bitmapHeightInDCV - currentHeightDCV) / 2;

        } else {
            extraCanvas.scale(scaleHeightRatio, scaleHeightRatio);

            val bitmapWidthInDCV = extraBitmap.width.toFloat() / scaleHeightRatio;
            offsetX = (bitmapWidthInDCV - currentWidthDCV) / 2;
        };

    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

//        Log.d(TAG, "DCV size: " + width + " " + height);
        currentWidthDCV = width;
        currentHeightDCV = height;

        if (::extraBitmap.isInitialized) {
            extraBitmap.recycle();
        }

        if(backgroundBitmap != null) {
            extraBitmap = Bitmap.createBitmap(backgroundBitmap);
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

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
//        Log.d(TAG, (event.x.toString() + " " + event.y.toString()))

        //add any offsets calculated for the canvas in the DCV
        motionTouchEventX = event.x + offsetX;
        motionTouchEventY = event.y + offsetY;

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart();
            MotionEvent.ACTION_MOVE -> touchMove();
            MotionEvent.ACTION_UP -> touchUp();
        }
        return true;
    }

    private fun touchStart()
    {
        //add a copy of the current bitmap to the undo stack
        val bitmap: Bitmap = Bitmap.createBitmap(extraBitmap);
        undoStack.push(bitmap);

        //clear the redo stack
        redoStack.clear();

        path.reset();
        path.moveTo(motionTouchEventX, motionTouchEventY);
        currentX = motionTouchEventX;
        currentY = motionTouchEventY;
    }

    private fun touchMove()
    {
        val dx = Math.abs(motionTouchEventX - currentX);
        val dy = Math.abs(motionTouchEventY - currentY);
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2);
            currentX = motionTouchEventX;
            currentY = motionTouchEventY;

            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint);
        }
        invalidate();
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset();
    }

    fun undoDraw() {
        try {
            //get the bitmap which is the previous state
            val prevBitmap: Bitmap = undoStack.pop();

            //save the current bitmap as a redo state
            val currentBitmap: Bitmap = Bitmap.createBitmap(extraBitmap);
            redoStack.push(currentBitmap);

            val width = prevBitmap.width;
            val height = prevBitmap.height;

            //copy the previous bitmap into extraBitmap, which will update the Canvas in onDraw()
            val pixels = IntArray(width * height);
            prevBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            extraBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (e: EmptyStackException) {
            //do nothing
            Log.i(TAG, "Cannot UNDO, the redo stack is empty.")
        }
    }

    fun redoDraw() {
        try {
            //get the bitmap which is the redo state
            val redoBitmap: Bitmap = redoStack.pop();

            //add a copy of the current bitmap to the undo stack
            val bitmap: Bitmap = Bitmap.createBitmap(extraBitmap);
            undoStack.push(bitmap);

            val width = redoBitmap.width;
            val height = redoBitmap.height;

            //copy the previous bitmap into extraBitmap, which will update the Canvas in onDraw()
            val pixels = IntArray(width * height);
            redoBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            extraBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (e: EmptyStackException) {
            //do nothing
            Log.i(TAG, "Cannot REDO, the redo stack is empty.")
        }
    }

    fun clearDrawings() {
        //add a copy of the current bitmap to the undo stack
        val bitmap: Bitmap = Bitmap.createBitmap(extraBitmap);
        undoStack.push(bitmap);

        //restore the original background
        setBackgroundImage(backgroundBitmap);
    }

    fun saveCurrentBitmap(filename: String) {
        saveBitmapToFileStorage(extraBitmap, filename, context);
    }

    fun saveOriginalBitmap(filename: String) {
        saveBitmapToFileStorage(backgroundBitmap, filename, context);
    }

    companion object {
        private const val TAG = "DrawableCanvasView"
    }

    class LimitedSizeStack<T>(private val maxSize: Int) : Stack<T>() {
        override fun push(item: T): T {
            if (size >= maxSize) {
                removeAt(0)
            }
            return super.push(item)
        }
    }
}