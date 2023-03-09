package com.kylearon.fgcaddie.coursenotes

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.R

class NewCourseDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_fragment_new_course, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
                        // create the new course
                    })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                        getDialog().cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "NewCourseDialog"
    }
}