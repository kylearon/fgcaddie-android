package com.kylearon.fgcaddie.hole

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Hole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditHoleDialogFragment(hole: Hole, recyclerView: RecyclerView, view: View) : DialogFragment() {

    private val hole: Hole = hole;
    private val parentRecyclerView: RecyclerView = recyclerView;
    private val parentView: View = view;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_edit_hole, null);

            //listen to changes from the holes number ToggleGroup
            val buttonGroup = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.parToggleButton);
            buttonGroup.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
                // Respond to button selection
                Log.d(TAG, " button: " + checkedId);
            }

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the course name
                    val distanceInput = dialogView.findViewById<EditText>(R.id.distance_input);
                    val distanceString = distanceInput.text.toString();
                    Log.d(TAG, " the hole distance is: " + distanceString);

                    //get the selected holes button
                    val checkedButtonId = buttonGroup.checkedButtonId;
                    val parButtonText = dialogView.findViewById<Button>(checkedButtonId).text.toString();
                    val parNumber = parButtonText.toInt();
                    Log.d(TAG, " the hole par is: " + parNumber);

                    //update the hole data
                    hole.par = parNumber;
                    hole.length = distanceString.toInt();

                    //add the hole to the repository
                    MainActivity.ServiceLocator.getCourseRepository().updateHole(hole);

                    //create the action and navigate to the hole fragment. this will refresh the fragment and get updated data
                    val action = HolePageFragmentDirections.actionHolePageFragmentSelf(hole = Json.encodeToString(hole));
                    parentView!!.findNavController().navigate(action);

                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "EditHoleDialog"
    }
}