package com.kylearon.fgcaddie.coursenotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.R

class CourseNotesAdapter : RecyclerView.Adapter<CourseNotesAdapter.CourseNotesViewHolder>() {

    private val courseList = listOf<String>("Georgetown", "Fox Hills", "Royal Scot");

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class CourseNotesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button = view.findViewById<Button>(R.id.button_item)
    }

    /**
     * Returns the number of items this view will show
     */
    override fun getItemCount(): Int {
        return courseList.size;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseNotesViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_item_view, parent, false);

        return CourseNotesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: CourseNotesViewHolder, position: Int) {
        val item = courseList.get(position);
        holder.button.text = item.toString();

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
        holder.button.setOnClickListener {

        }
    }

}