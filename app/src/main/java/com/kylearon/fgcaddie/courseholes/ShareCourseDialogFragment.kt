package com.kylearon.fgcaddie.courseholes

import android.animation.ObjectAnimator
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.utils.FileUtils.Companion.getPrivateAppStorageFilepath
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.File

class ShareCourseDialogFragment(courseId: String, view: View) : DialogFragment() {

    private var course: Course? = null;

    private var loading: Boolean = false;

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
            val dialogView = inflater.inflate(R.layout.dialog_fragment_share_course, null);


            //add the view to the dialog and create the dialog
            val dialog = builder.setView(dialogView)
                //set null as the listener so it won't automatically close the dialog when clicked
                .setPositiveButton(R.string.ok, null)
                .create();

            //put the current tag into the dialog if it exists
            dialogView.findViewById<EditText>(R.id.course_tag_input).setText(course!!.tag);
            

            dialog.setOnShowListener {
                val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    // Your code here
                    // The dialog won't dismiss unless you call dialog.dismiss()

                    //hide the ok button
                    button.visibility = View.GONE;

                    if(loading)
                    {
                        //dismiss the dialog
                        dialog.dismiss();
                    }
                    else
                    {
                        //get the tags
                        val courseTagInput = dialogView.findViewById<EditText>(R.id.course_tag_input);
                        val courseTagString = courseTagInput.text.toString();
                        Log.d(TAG, " the course tags are: " + courseTagString);
                        course!!.tag = courseTagString;

                        //get the password to set for the course
                        val coursePasswordInput = dialogView.findViewById<EditText>(R.id.course_password_input);
                        val coursePasswordString = coursePasswordInput.text.toString();
                        Log.d(TAG, " the course password is: " + coursePasswordString);
                        course!!.password = coursePasswordString;

                        //hide the previous layout elements
                        dialogView.findViewById<LinearLayout>(R.id.course_password_input_layout).visibility = View.GONE;
                        dialogView.findViewById<LinearLayout>(R.id.course_tag_input_layout).visibility = View.GONE;
                        dialogView.findViewById<TextView>(R.id.share_course_dialog_title).visibility = View.GONE;

                        //show the loading bar and status message
                        dialogView.findViewById<TextView>(R.id.course_json_message).visibility = View.VISIBLE;
                        dialogView.findViewById<RelativeLayout>(R.id.loading_progress_bar_layout).visibility = View.VISIBLE;


                        //create the course json to send
                        var courseJson = Json.encodeToString(course);
                        Log.i(TAG, "SENDING courseJson");
                        Log.i(TAG, courseJson);

                        uploadCourse(dialogView, dialog, courseJson, "false");

                    }

                }
            }

            return dialog;

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun uploadCourse(dialogView: View, dialog: Dialog, courseJson: String, overwrite: String) {

        //send the course json to the backend
        (requireActivity() as AppCompatActivity).lifecycleScope.launch {
            try {

                //set this as loading
                loading = true;

                val responseForPOSTJson: HttpResponse = MainActivity.ServiceLocator.getHttpClient().post {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = MainActivity.StaticVals.RAILWAY_URL
                        path("api/course")
                        parameters.append("api-key", "android")
                        parameters.append("overwrite", overwrite)
                    }

                    //add body
                    contentType(ContentType.Application.Json);
                    setBody(courseJson);
                }

                Log.i(TAG, "Response status: ${responseForPOSTJson.status}")
                Log.i(TAG, "Response body: ${responseForPOSTJson.bodyAsText()}")

                when(responseForPOSTJson.status) {
                    HttpStatusCode.Created -> {

                        if(isAdded) {
                            //update the status messages
                            dialogView.findViewById<TextView>(R.id.course_json_message).text = requireContext().getString(R.string.upload_course_json_success_message);
                            dialogView.findViewById<TextView>(R.id.course_images_message).visibility = View.VISIBLE;
                        }

                        Log.i(TAG, "Uploading images to backend now that Course upload has succeeded.");
                        uploadImages(dialogView);
                    }
                    HttpStatusCode.BadRequest -> {

                        Log.i(TAG, "BAD REQUEST");

                        if(isAdded) {

                            val jsonElement: JsonElement = Json.parseToJsonElement(responseForPOSTJson.bodyAsText());
                            if (jsonElement is JsonObject) {
                                // Use it as JsonObject
                                val jsonObject = jsonElement as JsonObject;
                                println(jsonObject["error"]);

                                val errorMessage = jsonObject["error"].toString();

                                //update the status messages for know body
                                dialogView.findViewById<TextView>(R.id.course_json_message).text = requireContext().getString(R.string.upload_course_json_failed_message) + "\n\n" + errorMessage;
                                dialogView.findViewById<RelativeLayout>(R.id.loading_progress_bar_layout).visibility = View.GONE;

                                if(errorMessage.contains("GUID already exists")) {
                                    //ask if want to push anyhow
                                    dialogView.findViewById<LinearLayout>(R.id.force_upload_layout).visibility = View.VISIBLE;

                                    //listen for buttons
                                    dialogView.findViewById<Button>(R.id.yes_button).setOnClickListener {

                                        //hide the force upload messages
                                        dialogView.findViewById<LinearLayout>(R.id.force_upload_layout).visibility = View.GONE;
                                        dialogView.findViewById<TextView>(R.id.course_images_message).visibility = View.GONE;

                                        //force the upload by adding the overwrite param to the url
                                        (requireActivity() as AppCompatActivity).lifecycleScope.launch {
                                            uploadCourse(dialogView, dialog, courseJson, "true");
                                        }
                                    }

                                    dialogView.findViewById<Button>(R.id.no_button).setOnClickListener {
                                        //dismiss the dialog
                                        dialog.dismiss();
                                    }
                                }
                            }
                            else {
                                //update the status messages for unknown body
                                dialogView.findViewById<TextView>(R.id.course_json_message).text = requireContext().getString(R.string.upload_course_json_failed_message) + "\n\n" + responseForPOSTJson.bodyAsText();
                                dialogView.findViewById<RelativeLayout>(R.id.loading_progress_bar_layout).visibility = View.GONE;
                            }

                        }
                    }

                }

            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage)
                loading = false;
            }
        }
    }

    private suspend fun uploadImages(dialogView: View) {

        try {
            val responseForPOSTImages: HttpResponse = MainActivity.ServiceLocator.getHttpClient().post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = MainActivity.StaticVals.RAILWAY_URL
                    path("api/images")
                    parameters.append("api-key", "android")
                }
                setBody(MultiPartFormDataContent(
                    formData {
                        // append("description", "images from android app")

                        var imageCount = 0;

                        //append each shot from each hole
                        course?.holes?.forEach {hole ->

                            hole.shots_tee?.forEach { shot ->

                                //construct the filepaths and get the files
                                val filepathOriginal = getPrivateAppStorageFilepath(shot.image_original);
                                val filepathMarkedup = getPrivateAppStorageFilepath(shot.image_markedup);

                                Log.i(TAG, "Adding file to POST: " + filepathOriginal);
                                Log.i(TAG, "Adding file to POST: " + filepathMarkedup);

                                //append the shot image bytes to the images form data list
                                append(
                                    "images",
                                    File(filepathOriginal).readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/png")
                                        append(
                                            HttpHeaders.ContentDisposition,
                                            "filename=${shot.image_original}"
                                        )
                                    }
                                )

                                append(
                                    "images",
                                    File(filepathMarkedup).readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/png")
                                        append(
                                            HttpHeaders.ContentDisposition,
                                            "filename=${shot.image_markedup}"
                                        )
                                    }
                                )

                                //update the status message with how many images are being uploaded
                                imageCount = imageCount + 2;
                                if(isAdded) {
                                    dialogView.findViewById<TextView>(R.id.course_images_message).text = "Uploading $imageCount course images please wait...";
                                }
                            }
                        }

                        if(isAdded) {
                            //Set the animation progress bar going. estimate 1500ms per image for now
                            val duration = (imageCount * 1500).toLong();
                            val progressBar = dialogView.findViewById<ProgressBar>(R.id.loading_progress_bar);
                            ObjectAnimator.ofInt(progressBar, "progress", 100)
                                .setDuration(duration)
                                .start()
                        }

                    },
                    boundary = "WebAppBoundary"
                )
                )
            }

            Log.i(TAG, "Response status: ${responseForPOSTImages.status}");
            Log.i(TAG, "Response body: ${responseForPOSTImages.bodyAsText()}");

            if(isAdded) {
                //update status messages
                dialogView.findViewById<TextView>(R.id.course_images_message).text = requireContext().getString(R.string.upload_course_images_success_message);
                dialogView.findViewById<RelativeLayout>(R.id.loading_progress_bar_layout).visibility = View.GONE;
            }

        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        }

    }


    companion object {
        const val TAG = "ShareCourseDialog"
    }
}
