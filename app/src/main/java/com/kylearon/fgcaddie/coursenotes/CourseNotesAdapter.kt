package com.kylearon.fgcaddie.coursenotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.HomePageFragmentDirections
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseNotesAdapter : RecyclerView.Adapter<CourseNotesAdapter.CourseNotesViewHolder>() {

    private var view: View? = null;

//    private val courseList = listOf<String>("Georgetown", "Fox Hills", "Royal Scot");

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
    class CourseNotesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val layoutClickable = view.findViewById<LinearLayout>(R.id.course_notes_row_item);
        val courseLabel = view.findViewById<TextView>(R.id.course_label);
        val courseCreator = view.findViewById<TextView>(R.id.course_creator);
        val courseDate = view.findViewById<TextView>(R.id.course_date);
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseNotesViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.course_notes_row_item, parent, false);

        view = layout;

        return CourseNotesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: CourseNotesViewHolder, position: Int) {
        val item: Course = courses.get(position);
        holder.courseLabel.text = item.name;
        holder.courseCreator.text = item.creator;
        holder.courseDate.text = item.date_created;

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
        holder.layoutClickable.setOnClickListener {
            //create the action and navigate to the course notes fragment
            val action = CourseNotesPageFragmentDirections.actionCourseNotesPageFragmentToCourseHolesPageFragment( courseid = item.guid);
            view!!.findNavController().navigate(action);

        }
    }

}