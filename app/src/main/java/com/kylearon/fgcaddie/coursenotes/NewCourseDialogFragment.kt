package com.kylearon.fgcaddie.coursenotes

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot
import com.kylearon.fgcaddie.utils.FileUtils.Companion.getDatetimeReadable
import java.text.SimpleDateFormat
import java.util.*

class NewCourseDialogFragment(parentView: RecyclerView) : DialogFragment() {

    private val parentRecyclerView: RecyclerView = parentView;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_new_course, null);

            //listen to changes from the holes number ToggleGroup
            val buttonGroup = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.holesToggleButton);
            buttonGroup.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
                // Respond to button selection
                Log.d(TAG, " button: " + checkedId);
            }

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the course name
                    val courseNameInput = dialogView.findViewById<EditText>(R.id.course_name_input);
                    val courseNameString = courseNameInput.text.toString();
                    Log.d(TAG, " the course name is: " + courseNameString);

                    //get the selected holes button
                    val checkedButtonId = buttonGroup.checkedButtonId;
                    val holesButtonText = dialogView.findViewById<Button>(checkedButtonId).text.toString();
                    val holesNumber = holesButtonText.toInt();
                    Log.d(TAG, " the holes are: " + holesButtonText);

                    //get the creator
                    val creator = MainActivity.ServiceLocator.getSettingsRepository().getSettings()!!.user_name;

                    //get the datetime
                    val currentDate = getDatetimeReadable();

                    //create the new course
                    val newCourse = Course(UUID.randomUUID().toString(), courseNameString, creator, currentDate, "", "", ArrayList<Hole>());

                    //init each hole for the new course
                    for(i in 1 .. holesNumber) {
                        newCourse.holes.add(Hole(UUID.randomUUID().toString(), newCourse.guid, i, 3, 0, ArrayList<Shot>(), ArrayList<Shot>(), ArrayList<Shot>()));
                    }

                    //add the course to the repository
                    MainActivity.ServiceLocator.getCourseRepository().addCourse(newCourse);

                    //refresh the parent view's data adapter
                    val adapter = CourseNotesAdapter();
                    adapter.updateData(MainActivity.ServiceLocator.getCourseRepository().getCourses());
                    parentRecyclerView.adapter = adapter;

                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "NewCourseDialog"
    }
}