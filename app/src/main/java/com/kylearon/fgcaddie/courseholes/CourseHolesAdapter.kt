package com.kylearon.fgcaddie.courseholes

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import kotlinx.serialization.json.Json

import kotlinx.serialization.encodeToString
import java.io.File

class CourseHolesAdapter(courseId: String) : RecyclerView.Adapter<CourseHolesAdapter.CourseHolesViewHolder>() {

    private var parentView: View? = null;

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
        val holeButtonNumberTextView = view.findViewById<TextView>(R.id.hole_button_number)
        val holeRow = view.findViewById<LinearLayout>(R.id.hole_row);

        val noPhotosMessage = view.findViewById<TextView>(R.id.no_photos_message);

        val photosInnerRow = view.findViewById<LinearLayout>(R.id.photos_inner_row);

        val parTextView = view.findViewById<TextView>(R.id.hole_par_text);

        val distanceTextView = view.findViewById<TextView>(R.id.hole_distance_text);
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
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.hole_button_row_view, parent, false);
        parentView = parent;
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
        holder.holeButtonNumberTextView.text = item.hole_number.toString();

        //set the par string in the UI
        val parTextString = "Par " + item.par.toString();
        holder.parTextView.text = parTextString;

        //set the yds string in the UI
        val distanceTextString = item.length.toString() + " yds";
        holder.distanceTextView.text = distanceTextString;

        //hide the no photos message
        if(item.shots_tee.size > 0) {
            holder.noPhotosMessage.visibility = View.GONE;
        }

        //set the inner photos
        item.shots_tee.forEach { s ->
            //construct the filepath and get the file
            val imageFilename = s.image_markedup;
//            val filepath = "file:///storage/emulated/0/Pictures/FGCaddie/" + imageFilename + ".png";
            val filepath = "file:///data/user/0/com.kylearon.fgcaddie/files/" + imageFilename + ".png";

            //put the photo into the inflated ImageView
            val imageViewLayout = LayoutInflater.from(holder.photosInnerRow.context).inflate(R.layout.mini_photo, holder.photosInnerRow, false);
            imageViewLayout.findViewById<ImageView>(R.id.mini_photo_image_view).load(Uri.parse(filepath));
            holder.photosInnerRow.addView(imageViewLayout)
        }

        // Assigns a [OnClickListener] to the LinearLayout contained in the [ViewHolder]
        holder.holeRow.setOnClickListener {
//            Log.d("CourseHolesAdapter", "CLICKED A HOLE: " + item.hole_number);
            //create the action and navigate to the hole fragment
            val action = CourseHolesPageFragmentDirections.actionCourseHolesPageFragmentToHolePageFragment(hole = Json.encodeToString(item));
            parentView!!.findNavController().navigate(action);
        }
    }

}