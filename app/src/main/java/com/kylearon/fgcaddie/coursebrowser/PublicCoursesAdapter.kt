package com.kylearon.fgcaddie.coursebrowser

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

open class PublicCoursesAdapter(fragmentActivity: FragmentActivity) : RecyclerView.Adapter<PublicCoursesAdapter.PublicCoursesViewHolder>() {

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

                    //save the course json
                    val courseJson: Course = Json.decodeFromString<Course>(response.bodyAsText());
                    MainActivity.ServiceLocator.getCourseRepository().addCourse(courseJson);

                    downloadedCourseJson = courseJson;

                    Log.i(TAG, "SAVED COURSE JSON");

                    notifyDataSetChanged();

                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage)
                }
            }.invokeOnCompletion {

                //download the images for this course json
                (fragmentActivity as AppCompatActivity).lifecycleScope.launch {
                    if(downloadedCourseJson != null) {
                        Log.i(TAG, "Downloading Course Images...");
                        getImages(downloadedCourseJson!!);
                    }
                }

            }

        }

    }

    private suspend fun getImages(course: Course) = withContext(Dispatchers.IO) {

        //download the images for the course
        course?.holes?.forEach {hole ->

            hole.shots_tee?.forEach { shot ->

                //construct the url and get the file
                val imageFilename = shot.image_markedup;
                val imageUrl = MainActivity.StaticVals.AWS_URL + "/" + imageFilename;

                //get the image from the url
                Log.i(TAG, "Downloading Image: " + imageUrl);
                downloadAndSaveImageFromUrl(imageUrl, imageFilename);
            }
        }

    }

    private fun downloadAndSaveImageFromUrl(url: String, fileName: String) {
        try {
            val imageUrl = URL(url);
            val connection = imageUrl.openConnection() as HttpURLConnection;
            connection.connect();

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP error code: ${connection.responseCode}");
            }

            val inputStream = connection.inputStream;
            val bufferedInputStream = BufferedInputStream(inputStream);
            val file = File(fragmentActivity.filesDir, fileName);
            val fileOutputStream = FileOutputStream(file);
            val bufferedOutputStream = BufferedOutputStream(fileOutputStream);

            val buffer = ByteArray(4096);
            var bytesRead: Int;
            while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }

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