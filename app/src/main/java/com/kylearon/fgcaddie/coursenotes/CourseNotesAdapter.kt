package com.kylearon.fgcaddie.coursenotes

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course

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

        val coursePar = view.findViewById<TextView>(R.id.course_par);
        val courseLength = view.findViewById<TextView>(R.id.course_length);

        val courseCreator = view.findViewById<TextView>(R.id.course_creator);
        val courseDate = view.findViewById<TextView>(R.id.course_date);

        val scorecardFront = view.findViewById<LinearLayout>(R.id.scorecard_front);
        val scorecardBack = view.findViewById<LinearLayout>(R.id.scorecard_back)

        val holeNumberRowFront = view.findViewById<LinearLayout>(R.id.hole_number_row_front);
        val holeNumberRowBack = view.findViewById<LinearLayout>(R.id.hole_number_row_back);

        val parRowFront = view.findViewById<LinearLayout>(R.id.par_row_front);
        val parRowBack = view.findViewById<LinearLayout>(R.id.par_row_back);

        val lengthRowFront = view.findViewById<LinearLayout>(R.id.length_row_front);
        val lengthRowBack = view.findViewById<LinearLayout>(R.id.length_row_back);
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
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_course_notes_row, parent, false);

        view = layout;

        return CourseNotesViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: CourseNotesViewHolder, position: Int) {
        val item: Course = courses.get(position);

        if(item.holes.size == 9) {
            //show both front and back scorecards
            holder.scorecardBack.visibility = View.GONE;
        }

        //count the par and yards
        var par = 0;
        var length = 0;
        var index = 0;

        item.holes.forEach {
            par += it.par;
            length += it.length;

            if(index < 9) {
                (holder.parRowFront[index] as TextView).text = it.par.toString();
                (holder.lengthRowFront[index] as TextView).text = it.length.toString();
            } else if (index < 18) {
                (holder.parRowBack[index - 9] as TextView).text = it.par.toString();
                (holder.lengthRowBack[index -  9] as TextView).text = it.length.toString();
            }

            index++;
        }


        holder.courseLabel.text = item.name;
        holder.coursePar.text = "Par $par";
        holder.courseLength.text = "$length yards";
        holder.courseCreator.text = item.creator;
        holder.courseDate.text = item.date_created;

        if(item.color.isNotEmpty()) {
            holder.courseLabel.setTextColor(Color.parseColor(item.color.toString()));
            (holder.holeNumberRowFront.background as GradientDrawable).setColor(Color.parseColor(item.color.toString()));
            (holder.holeNumberRowBack.background as GradientDrawable).setColor(Color.parseColor(item.color.toString()));
        } else {
            holder.courseLabel.setTextColor(Color.parseColor("#535353"));
            (holder.holeNumberRowFront.background as GradientDrawable).setColor(Color.parseColor("#535353"));
            (holder.holeNumberRowBack.background as GradientDrawable).setColor(Color.parseColor("#535353"));
        }


        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
        holder.layoutClickable.setOnClickListener {
            //create the action and navigate to the course notes fragment
            val action = CourseNotesPageFragmentDirections.actionCourseNotesPageFragmentToCourseHolesPageFragment( courseid = item.guid);
            view!!.findNavController().navigate(action);

        }
    }

}