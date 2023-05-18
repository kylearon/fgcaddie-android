package com.kylearon.fgcaddie.courseholes

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ShareCourseDialogFragment(courseId: String, view: View) : DialogFragment() {

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
            val dialogView = inflater.inflate(R.layout.dialog_fragment_share_course, null);


            //add the view to the dialog and create the dialog
            builder.setView(dialogView)
                //action buttons and callbacks
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

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

                    //create the course json to send
                    var courseJson = Json.encodeToString(course);
                    Log.i(TAG, "SENDING courseJson");
                    Log.i(TAG, courseJson);

                    //send the course json to the backend
                    (requireActivity() as AppCompatActivity).lifecycleScope.launch {
                        try {
                            val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().post {
                                url {
                                    protocol = URLProtocol.HTTPS
                                    host = MainActivity.StaticVals.RAILWAY_URL
                                    path("api/course")
                                    parameters.append("api-key", "android")
                                }

                                //add body
                                contentType(ContentType.Application.Json);
                                setBody(courseJson);
                            }

                            Log.i(TAG, "Response status: ${response.status}")
                            Log.i(TAG, "Response body: ${response.bodyAsText()}")

                            when(response.status) {
                                HttpStatusCode.Created -> {

                                    Log.i(TAG, "Uploading images to backend now that Course upload has succeeded.");

                                        try {
                                            val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().post {
                                                url {
                                                    protocol = URLProtocol.HTTPS
                                                    host = MainActivity.StaticVals.RAILWAY_URL
                                                    path("api/images")
                                                    parameters.append("api-key", "android")
                                                }
                                                setBody(MultiPartFormDataContent(
                                                    formData {
                                                        // append("description", "images from android app")

                                                        //append each shot from each hole
                                                        course?.holes?.forEach {hole ->

                                                            hole.shots_tee?.forEach { shot ->

                                                                //construct the filepath and get the file
                                                                val imageFilename = shot.image_markedup;
                                                                val filepath = "/data/user/0/com.kylearon.fgcaddie/files/" + imageFilename;

                                                                Log.i(TAG, "Adding file to POST: " + filepath);

                                                                //append the shot image bytes to the images form data list
                                                                append(
                                                                    "images",
                                                                    File(filepath).readBytes(),
                                                                    Headers.build {
                                                                        append(HttpHeaders.ContentType, "image/png")
                                                                        append(
                                                                            HttpHeaders.ContentDisposition,
                                                                            "filename=${shot.image_markedup}"
                                                                        )
                                                                    }
                                                                )

                                                            }
                                                        }

                                                    },
                                                    boundary = "WebAppBoundary"
                                                )
                                                )
                                            }

                                            Log.i(TAG, "Response status: ${response.status}")
                                            Log.i(TAG, "Response body: ${response.bodyAsText()}")
                                        } catch (e: Exception) {
                                            Log.e(TAG, e.localizedMessage)
                                        }

                                }
                                HttpStatusCode.BadRequest -> {

                                    //TODO: show a dialog saying it was not uploaded?

                                }

                            }

                        } catch (e: Exception) {
                            Log.e(TAG, e.localizedMessage)
                        }
                    }

                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
//                    getDialog().cancel()
                })
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }


    companion object {
        const val TAG = "ShareCourseDialog"
    }
}
