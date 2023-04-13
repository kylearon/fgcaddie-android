package com.kylearon.fgcaddie.courseholes

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ShareCourseDialogFragment(courseId: String, view: View) : DialogFragment() {

    private var course: Course? = null;


    private val courseId = courseId;
    private val view = view;

    init {
        course = MainActivity.ServiceLocator.getCourseRepository().getCourse(courseId);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirm_share_course_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                //remove the course
//                MainActivity.ServiceLocator.getCourseRepository().removeCourse(courseId);

                //create the course json to send
                var courseJson = Json.encodeToString(course);
                Log.i(TAG, "courseJson");
                Log.i(TAG, courseJson);

                //TODO: send the json
//                val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().request("https://ktor.io/") {
//                    method = HttpMethod.Post
//                }

//                val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().get {
//                    url {
//                        protocol = URLProtocol.HTTPS
//                        host = "ktor.io"
//                        path("docs/welcome.html")
//                    }
//                }

//                val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().submitFormWithBinaryData(
//                    url = "http://localhost:8080/upload",
//                    formData = formData {
//                        append("description", "Ktor logo")
//                        append("image", File("ktor_logo.png").readBytes(), Headers.build {
//                            append(HttpHeaders.ContentType, "image/png")
//                            append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
//                        })
//                    }
//                )


                //test send some images to POST api/images
                (requireActivity() as AppCompatActivity).lifecycleScope.launch {

                    try {
                        val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().post {
                            url {
                                protocol = URLProtocol.HTTPS
                                host = "expressjs-postgres-production-3edc.up.railway.app"
                                path("api/images")
                                parameters.append("api-key", "android")
                            }
                            setBody(MultiPartFormDataContent(
                                formData {
                                    // append("description", "images from android app")

                                    //append each shot from the first hole as a test
                                    course?.holes?.get(0)?.shots_tee?.forEach { shot ->

                                        //construct the filepath and get the file
                                        val imageFilename = shot.image_markedup;
                                        val filepath = "/data/user/0/com.kylearon.fgcaddie/files/" + imageFilename;

                                        Log.i(TAG, "Adding file to POST: " + filepath);

                                        append("images", File(filepath).readBytes(), Headers.build {
                                        // append("images", File(Uri.parse(filepath).path).readBytes(), Headers.build {
                                            append(HttpHeaders.ContentType, "image/png")
                                            append(HttpHeaders.ContentDisposition,"filename=${shot.image_markedup}")
                                        })
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

                //TODO: create the zip file of images to send



                //TODO: send the images zip


                //TODO: navigate to the correct page



                //navigate back a page
                //create the action and navigate to the course notes fragment
//                val action = CourseHolesPageFragmentDirections.actionCourseHolesPageFragmentToCourseNotesPageFragment();
//                view.findNavController().navigate(action);
            }
            .setNegativeButton(getString(R.string.cancel)) { _,_ -> }
            .create()

    companion object {
        const val TAG = "ShareCourseDialog"
    }
}