package com.kylearon.fgcaddie.home

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Settings

class SettingsDialogFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val dialogView = inflater.inflate(R.layout.dialog_fragment_settings, null);

            dialogView.findViewById<EditText>(R.id.user_name_input).setText(MainActivity.ServiceLocator.getSettingsRepository().getSettings()!!.user_name);

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                    //get the user_name setting
                    val userNameInput = dialogView.findViewById<EditText>(R.id.user_name_input);
                    val userNameString = userNameInput.text.toString();
                    Log.d(TAG, " the user name is: " + userNameString);

                    //save the new settings
                    var newSettings = Settings(userNameString);
                    MainActivity.ServiceLocator.getSettingsRepository().setSettings(newSettings);

                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    getDialog()!!.cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "SettingsDialog"
    }
}