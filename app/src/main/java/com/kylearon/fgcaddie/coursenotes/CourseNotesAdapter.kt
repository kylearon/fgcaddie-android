package com.kylearon.fgcaddie.coursenotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    init {
        GlobalScope.launch {
            courses.addAll(MainActivity.ServiceLocator.getCourseRepository().fetchCourses());
        }
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class CourseNotesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.button_item);
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
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_item_view, parent, false);

        view = layout;

        return CourseNotesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: CourseNotesViewHolder, position: Int) {
        val item: Course = courses.get(position);
        holder.button.text = item.name;

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
        holder.button.setOnClickListener {
            //create the action and navigate to the course notes fragment
            val action = CourseNotesPageFragmentDirections.actionCourseNotesPageFragmentToCourseHolesPageFragment( courseid = item.guid);
            view!!.findNavController().navigate(action);

        }
    }

}