package com.kylearon.fgcaddie.coursebrowser

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.kylearon.fgcaddie.R

class DownloadCourseStatusDialogFragment()  : DialogFragment() {

    private var dialogView: View? = null;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it);
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            dialogView = inflater.inflate(R.layout.dialog_fragment_download_course_status, null);

            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
//                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
//
//                    //TODO: navigate back?
//
//                })
//                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
////                    getDialog().cancel()
//                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun showImagesDownloadStatus() {

        if(dialogView != null) {
            dialogView!!.findViewById<TextView>(R.id.course_json_message).text = getString(R.string.download_course_json_success_message);
            dialogView!!.findViewById<TextView>(R.id.course_images_message).visibility = View.VISIBLE;

            //Set the animation progress bar going. estimate 1500ms per image for now
            val imageCount = 10;
            val duration = (imageCount * 1500).toLong();
            val progressBar = dialogView!!.findViewById<ProgressBar>(R.id.loading_progress_bar);
            ObjectAnimator.ofInt(progressBar, "progress", 100)
                .setDuration(duration)
                .start()
        }
    }

    fun finishImagesDownloadedStatus() {
        dialogView!!.findViewById<TextView>(R.id.course_images_message).text = getString(R.string.download_course_images_success_message);
        val progressBar = dialogView!!.findViewById<ProgressBar>(R.id.loading_progress_bar);
        progressBar.progress = 100;

        this.dismiss();
    }

    companion object {
        const val TAG = "NewCourseDialog"
    }
}