package com.kylearon.fgcaddie.camera

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot

class EditNoteDialogFragment(private val hole: Hole, private val shot: Shot, private val listener: EditNoteDialogListener): DialogFragment() {

    interface EditNoteDialogListener {
        fun onEditNoteSaved()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_edit_note, null);

            val noteEditText = dialogView.findViewById<EditText>(R.id.hole_note_input);
            noteEditText.setText(shot.note);

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the course name
                    val noteInput = dialogView.findViewById<EditText>(R.id.hole_note_input);
                    val noteString = noteInput.text.toString();

                    //update Hole
                    shot.note = noteString;
                    hole.updateShot(shot);
                    MainActivity.ServiceLocator.getCourseRepository().updateHole(hole);

                    //notify listeners
                    listener.onEditNoteSaved();
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "EditNoteDialog"
    }
}