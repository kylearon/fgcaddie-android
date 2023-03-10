package com.kylearon.fgcaddie.coursenotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseHolesAdapter(courseId: String) : RecyclerView.Adapter<CourseHolesAdapter.CourseHolesViewHolder>() {


    private var course: Course? = null;

    init {
//        GlobalScope.launch {
            course = MainActivity.ServiceLocator.getCourseRepository().getCourse(courseId);
//        }
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class CourseHolesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.button_item)
    }

    /**
     * Returns the number of items this view will show
     */
    override fun getItemCount(): Int {
        if(course == null) {
            return 0;
        }
        return course!!.holes.size;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseHolesViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_item_view, parent, false);

        return CourseHolesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: CourseHolesViewHolder, position: Int) {
        if(course == null) {
            return;
        }

        val item: Hole = course!!.holes.get(position);
        holder.button.text = item.hole_number.toString();

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
        holder.button.setOnClickListener {

        }
    }

}