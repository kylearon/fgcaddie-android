package com.kylearon.fgcaddie.courseholes

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course

class RenameCourseDialogFragment(courseId: String): DialogFragment() {

    private var course: Course? = null;

    init {
        course = MainActivity.ServiceLocator.getCourseRepository().getCourse(courseId);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_rename_course, null);

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the course name
                    val nameInput = dialogView.findViewById<EditText>(R.id.course_name_input);
                    val nameString = nameInput.text.toString();

                    //add the name to the repository
                    MainActivity.ServiceLocator.getCourseRepository().updateCourseName(course!!.guid, nameString);

                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "RenameCourseDialog"
    }
}