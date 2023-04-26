package com.kylearon.fgcaddie.shot

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix();
    private val prevMatrix = Matrix();

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener());
    private val gestureDetector = GestureDetector(context, GestureListener());

    init {
        scaleType = ScaleType.MATRIX;
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //ScaleGestureDetector handles multi-touch events
        scaleGestureDetector.onTouchEvent(event);

        //GestureDetector handles single-touch events
        gestureDetector.onTouchEvent(event);

        return true;
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private val mid = PointF();

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            matrix.set(prevMatrix);
            matrix.postScale(detector.scaleFactor, detector.scaleFactor, mid.x, mid.y);
            imageMatrix = matrix;
            prevMatrix.set(matrix);
            return true;
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            prevMatrix.set(matrix);
            midPoint(mid, detector);
            return true;
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            //nothing
        }

        private fun midPoint(point: PointF, detector: ScaleGestureDetector) {
            val x = detector.focusX
            val y = detector.focusY
            point.set(x, y)
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val start = PointF()

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Handle double-tap to reset the zoom
            imageMatrix = Matrix()
            prevMatrix.set(imageMatrix)
            matrix.set(imageMatrix)
            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            start.set(e.x, e.y)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

            // Only move when not in the middle of a pinch-to-zoom gesture
            if (!scaleGestureDetector.isInProgress) {
                val dx = e2.x - start.x
                val dy = e2.y - start.y
                matrix.set(imageMatrix)
                matrix.postTranslate(dx, dy)
                imageMatrix = matrix
                prevMatrix.set(matrix)
                start.set(e2.x, e2.y) // Update the starting point for the next drag
            }
            return true

        }

    }

    companion object {
        const val TAG = "ZoomableImageView"
    }
}