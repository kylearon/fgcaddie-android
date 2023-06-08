package com.kylearon.fgcaddie.coursebrowser

import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.camera.EditNoteDialogFragment
import com.kylearon.fgcaddie.courseholes.CourseHolesPageFragmentDirections
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Courses
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

open class PublicCoursesAdapter(fragmentActivity: FragmentActivity) : RecyclerView.Adapter<PublicCoursesAdapter.PublicCoursesViewHolder>() {

    private var parentView: View? = null;

    private val fragmentActivity = fragmentActivity;

    private var view: View? = null;

    private val courses = ArrayList<Course>();

    private var downloadedCourseJson: Course? = null;


    // Add a function to update the data and notify the RecyclerView
    fun updateData(newCourses: List<Course>) {
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class PublicCoursesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val courseLabel = view.findViewById<TextView>(R.id.course_label);
        val courseCreator = view.findViewById<TextView>(R.id.course_creator);
        val courseDate = view.findViewById<TextView>(R.id.course_date);
        val downloadClickable = view.findViewById<LinearLayout>(R.id.download_course_clickable);
        val downloadedText = view.findViewById<LinearLayout>(R.id.course_downloaded_text);
    }

    /**
     * Returns the number of items this view will show
     */
    override fun getItemCount(): Int {
        return courses.size;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicCoursesViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.course_browser_row_item, parent, false);

        parentView = parent;
        view = layout;

        return PublicCoursesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: PublicCoursesViewHolder, position: Int) {
        val item: Course = courses.get(position);

        holder.courseLabel.text = item.name;
        holder.courseCreator.text = item.creator;
        holder.courseDate.text = item.date_created;

        val existingCourse = MainActivity.ServiceLocator.getCourseRepository().getCourse(item.guid);
        if(existingCourse != null)
        {
            holder.downloadClickable.visibility = View.GONE;
            holder.downloadedText.visibility = View.VISIBLE;
        }

        holder.downloadClickable.setOnClickListener{

            //show a status dialog
            val downloadStatusDialog = DownloadCourseStatusDialogFragment();
            downloadStatusDialog.show(fragmentActivity.supportFragmentManager, TAG);

            //download the course json
            (fragmentActivity as AppCompatActivity).lifecycleScope.launch {
                try {
                    val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().get {
                        url {
                            protocol = URLProtocol.HTTPS
                            host = MainActivity.StaticVals.RAILWAY_URL
                            path("api/courses/" + item.guid)
                            parameters.append("api-key", "android")
                        }
                    }

                    Log.i(TAG, "Response status: ${response.status}")
                    Log.i(TAG, "Response body: ${response.bodyAsText()}")

                    //construct the Course object from the downloaded json
                    val courseObjectFromJson: Course = Json.decodeFromString<Course>(response.bodyAsText());

                    //make a copy and change the guids of the entire object and sub-objects with the deepCopy
                    val courseWithNewGuids = courseObjectFromJson.deepCopy();

                    //update the creator name trail
                    val creator = MainActivity.ServiceLocator.getSettingsRepository().getSettings()!!.user_name;
                    courseWithNewGuids.creator = creator;

                    //add the Course with new guids to the CourseRepository
                    MainActivity.ServiceLocator.getCourseRepository().addCourse(courseWithNewGuids);

                    //save the Course object to the class member variable
                    //we can use the updated Course object here because the images still point to the existing names
                    //upon downloading the images, new names will be generated for them.
                    //this will happen in the call to getImages(downloadedCourseJson) below
                    downloadedCourseJson = courseWithNewGuids;

                    Log.i(TAG, "SAVED COURSE JSON");

                    //notify the RecyclerView to update with the new repository data
                    notifyDataSetChanged();

                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage)
                }
            }.invokeOnCompletion {

                //show the status in the dialog
                downloadStatusDialog.showImagesDownloadStatus();


                //download the images for this course json
                (fragmentActivity as AppCompatActivity).lifecycleScope.launch {
                    if(downloadedCourseJson != null) {
                        Log.i(TAG, "Downloading Course Images...");
                        getImages(downloadedCourseJson!!);
                    }
                }.invokeOnCompletion {


                    //save the json
                    MainActivity.ServiceLocator.getCourseRepository().saveCourses();

                    //finish the progress bar
                    downloadStatusDialog.finishImagesDownloadedStatus();

                    //navigate back to courses page
//                    val action = CourseBrowserFragmentDirections.actionCourseBrowserPageFragmentToCourseNotesPageFragment();
//                    parentView!!.findNavController().navigate(action);
                }

            }

        }

    }

    private suspend fun getImages(course: Course) = withContext(Dispatchers.IO) {

        val guidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}".toRegex();

        //download the images for the course
        course?.holes?.forEach {hole ->

            hole.shots_tee?.forEach { shot ->

                //construct the url and get the file
                val imageMarkedupUrl = MainActivity.StaticVals.AWS_URL + "/" + shot.image_markedup;
                val imageOriginalUrl = MainActivity.StaticVals.AWS_URL + "/" + shot.image_original;

                //replace the old guid with the new guid for the new filename to save the image as
                val newGuid = shot.guid;
                val newImageMarkedupFilename = shot.image_markedup.replaceFirst(guidRegex, newGuid);
                val newImageOriginalFilename = shot.image_original.replaceFirst(guidRegex, newGuid);

                //get the image from the url
                Log.i(TAG, "Downloading Image: " + imageMarkedupUrl);
                downloadAndSaveImageFromUrl(imageMarkedupUrl, newImageMarkedupFilename);
                shot.image_markedup = newImageMarkedupFilename;

                //get the image from the url
//                Log.i(TAG, "Downloading Image: " + imageOriginalUrl);
//                downloadAndSaveImageFromUrl(imageOriginalUrl, newImageOriginalFilename);
//                shot.image_original = newImageOriginalFilename;

            }

        }

    }

    private fun downloadAndSaveImageFromUrl(url: String, fileName: String) {
        try {
            //create a connection to the image url
            val imageUrl = URL(url);
            val connection = imageUrl.openConnection() as HttpURLConnection;
            connection.connect();

            //make sure the connection is ok
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP error code: ${connection.responseCode}");
            }

            //create the input stream from the HttpURLConnection
            val inputStream = connection.inputStream;
            val bufferedInputStream = BufferedInputStream(inputStream);

            //create an empty file
            val file = File(fragmentActivity.filesDir, fileName);

            //create an output stream to the file
            val fileOutputStream = FileOutputStream(file);
            val bufferedOutputStream = BufferedOutputStream(fileOutputStream);

            //read the bytes from the input stream to the output stream
            val buffer = ByteArray(4096);
            var bytesRead: Int;
            while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }

            //close everything
            bufferedOutputStream.close();
            fileOutputStream.close();
            bufferedInputStream.close();
            inputStream.close();
            connection.disconnect();

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "PublicCoursesAdapter"
    }

}