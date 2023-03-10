package com.kylearon.fgcaddie.courseholes

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R

class ConfirmDeleteDialogFragment(courseId: String, view: View) : DialogFragment() {

    private val courseId = courseId;
    private val view = view;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirm_delete_course_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                //remove the course
                MainActivity.ServiceLocator.getCourseRepository().removeCourse(courseId);

                //navigate back a page
                //create the action and navigate to the course notes fragment
                val action = CourseHolesPageFragmentDirections.actionCourseHolesPageFragmentToCourseNotesPageFragment();
                view.findNavController().navigate(action);
            }
            .setNegativeButton(getString(R.string.cancel)) { _,_ -> }
            .create()

    companion object {
        const val TAG = "ConfirmDeleteDialog"
    }
}
