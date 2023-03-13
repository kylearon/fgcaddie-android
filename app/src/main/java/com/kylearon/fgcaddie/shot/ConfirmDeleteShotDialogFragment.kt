package com.kylearon.fgcaddie.shot

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Hole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConfirmDeleteShotDialogFragment(shotId: String, hole: Hole, view: View) : DialogFragment() {

    private val shotId = shotId;
    private val hole = hole;
    private val view = view;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirm_delete_shot_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->

                //remove the shot from the hole
                hole.shots_tee.removeIf { s -> s.guid.equals(shotId) }

                //update the Hole in the model
                MainActivity.ServiceLocator.getCourseRepository().updateHole(hole);

                //navigate back a page
                //create the action and navigate to the hole page fragment
                val action = ShotPageFragmentDirections.actionShotPageFragmentToHolePageFragment(hole = Json.encodeToString(hole));
                view.findNavController().navigate(action);
            }
            .setNegativeButton(getString(R.string.cancel)) { _,_ -> }
            .create()

    companion object {
        const val TAG = "ConfirmDeleteDialog"
    }
}
