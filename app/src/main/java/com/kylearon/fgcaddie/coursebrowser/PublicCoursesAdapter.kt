package com.kylearon.fgcaddie.coursebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course

class PublicCoursesAdapter : RecyclerView.Adapter<PublicCoursesAdapter.PublicCoursesViewHolder>() {

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


//        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
//        holder.button.setOnClickListener {
//            //create the action and navigate to the course notes fragment
//            val action = PublicCoursesFragmentDirections.actionCourseNotesPageFragmentToCourseHolesPageFragment( courseid = item.guid);
//            view!!.findNavController().navigate(action);
//
//        }
    }

}