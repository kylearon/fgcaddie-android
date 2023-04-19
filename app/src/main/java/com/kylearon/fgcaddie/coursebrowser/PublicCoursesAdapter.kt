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
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PublicCoursesAdapter(fragmentActivity: FragmentActivity) : RecyclerView.Adapter<PublicCoursesAdapter.PublicCoursesViewHolder>() {

    private val fragmentActivity = fragmentActivity;

    private var view: View? = null;

    private val courses = ArrayList<Course>();


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
                            host = "expressjs-postgres-production-3edc.up.railway.app"
                            path("api/courses/" + item.guid)
                            parameters.append("api-key", "android")
                        }
                    }

                    Log.i(TAG, "Response status: ${response.status}")
                    Log.i(TAG, "Response body: ${response.bodyAsText()}")

                    //save the course json
                    val courseJson = Json.decodeFromString<Course>(response.bodyAsText());
                    MainActivity.ServiceLocator.getCourseRepository().addCourse(courseJson);

                    Log.i(TAG, "SAVED COURSE JSON");

                    notifyDataSetChanged();

                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage)
                }
            }
        }

    }

    companion object {
        const val TAG = "PublicCoursesAdapter"
    }

}