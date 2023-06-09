package com.kylearon.fgcaddie.courseholes

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course

class EditCourseDialogFragment(courseId: String, adapter: CourseHolesAdapter): DialogFragment() {

    private var course: Course? = null;

    private lateinit var _adapter: CourseHolesAdapter;

    init {
        course = MainActivity.ServiceLocator.getCourseRepository().getCourse(courseId);
        _adapter = adapter;
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_edit_course, null);

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the course name
                    val nameInput = dialogView.findViewById<EditText>(R.id.course_name_input);
                    val nameString = nameInput.text.toString();

                    //get the course color
                    val colorInput = dialogView.findViewById<EditText>(R.id.course_color_input);
                    val colorString = colorInput.text.toString();

                    //save the name and color to the repository
                    MainActivity.ServiceLocator.getCourseRepository().updateCourseName(course!!.guid, nameString);
                    MainActivity.ServiceLocator.getCourseRepository().updateCourseColor(course!!.guid, colorString);

                    _adapter.updateData(course!!.guid)
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })

            //create and show the dialog
            val dialog: AlertDialog = builder.create()

            //init the course name
            dialogView.findViewById<EditText>(R.id.course_name_input).setText(course!!.name);

            if(course!!.color.isNullOrEmpty()) {
                course!!.color = "#DDDDDD"
            }

            //init the color picker elements
            updateColorEditText(dialogView, course!!.color)
            initColorPicker(dialogView)
            initSatLightnessPicker(dialogView)
            updateSatLightnessPicker(dialogView, course!!.color)

            return dialog;

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initColorPicker(dialogView: View) {
        val colors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED)
        val positions = floatArrayOf(0f, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f, 1f)

        val bmp = Bitmap.createBitmap(360, 1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint()
        paint.shader = LinearGradient(0f, 0f, bmp.width.toFloat(), 0f, colors, positions, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, bmp.width.toFloat(), 1f, paint)

        val colorPickerView = dialogView.findViewById<View>(R.id.color_picker)

        //TODO: fix this -> statically set the height and width because if you programmatically ask at this point it is 0,0
//        Log.i(TAG, "dialog w,h: " + colorPickerView.width + "," + colorPickerView.height)
//        Log.i(TAG, "color picker w,h: " + colorPickerView.width + "," + colorPickerView.height)
        colorPickerView.background = BitmapDrawable(resources, Bitmap.createScaledBitmap(bmp, 360, 50, true))

        colorPickerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val bitmap = (v.background as BitmapDrawable).bitmap
                val pixel = bitmap.getPixel(
                    (event.x / v.width * bitmap.width).toInt().coerceIn(0, bitmap.width - 1),
                    (event.y / v.height * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                )

                // Do something with the color
                val colorHex = "#${Integer.toHexString(pixel)}"

                Log.d("ColorPicker", "Color: $colorHex")

                updateColorEditText(dialogView, colorHex)

                updateSatLightnessPicker(dialogView, colorHex)
            }
            true
        }

    }

    private fun updateColorEditText(dialogView: View, argbHex: String) {
        val colorEditText = dialogView.findViewById<EditText>(R.id.course_color_input);
        colorEditText.setText(argbHex);
        colorEditText.setBackgroundColor(Color.parseColor(argbHex));
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSatLightnessPicker(dialogView: View) {

        val saturationLightnessPicker = dialogView.findViewById<View>(R.id.saturation_lightness_picker)
        saturationLightnessPicker.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val bitmap = (v.background as BitmapDrawable).bitmap
                val pixel = bitmap.getPixel(
                    (event.x / v.width * bitmap.width).toInt().coerceIn(0, bitmap.width - 1),
                    (event.y / v.height * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                )
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(pixel, hsl)
                // hsl[1] is the saturation, hsl[2] is the lightness
                // Do something with the saturation and lightness
//                Log.d("SaturationLightnessPicker", "Saturation: ${hsl[1]}, Lightness: ${hsl[2]}")

                val colorHex = "#${Integer.toHexString(pixel)}"
                updateColorEditText(dialogView, colorHex);
            }
            true
        }
    }

    private fun updateSatLightnessPicker(dialogView: View, argbHex: String) {

        val hue = getHueFromHex(argbHex)

        val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                val saturation = x.toFloat() / (bmp.width - 1)
                var lightness = 1f - y.toFloat() / (bmp.height - 1)
                // Adjust lightness based on position, smoothly transition from 0% to 100% to 0% to 50%
                val lightnessFactor = x.toFloat() / bmp.width
                lightness = lightness * (1 - lightnessFactor) + lightness / 2 * lightnessFactor
                val color = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
                bmp.setPixel(x, y, color)
            }
        }

        val saturationLightnessPicker = dialogView.findViewById<View>(R.id.saturation_lightness_picker)

        //TODO: fix this -> statically set the height and width because if you programmatically ask at this point it is 0,0
        saturationLightnessPicker.background = BitmapDrawable(resources, Bitmap.createScaledBitmap(bmp, 360, 64, true))
    }

    private fun getHueFromHex(hex: String): Float {
        val color = Color.parseColor(hex)
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)
        return hsl[0] // Hue is stored in the first index
    }

    companion object {
        const val TAG = "RenameCourseDialog"
    }
}