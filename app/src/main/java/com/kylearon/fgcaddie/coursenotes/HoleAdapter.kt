package com.kylearon.fgcaddie.coursenotes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HoleAdapter(hole: Hole) : RecyclerView.Adapter<HoleAdapter.HoleViewHolder>() {

    private var hole: Hole = hole;

    init {
//        GlobalScope.launch {
//            hole =
//        }
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class HoleViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
//        val holeButtonNumberTextView = view.findViewById<TextView>(R.id.hole_button_number)
    }

    /**
     * Returns the number of items this view will show
     */
    override fun getItemCount(): Int {
//        if(course == null) {
//            return 0;
//        }
//        return course!!.holes.size;
        //TODO: fix when we are displaying photos
        return 0;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_hole_picture_view, parent, false);

        return HoleViewHolder(layout);
    }

    /**
     * Replaces the content of the existing view at the given position with new data from the list at the given position
     */
    override fun onBindViewHolder(holder: HoleViewHolder, position: Int) {
        if(hole == null) {
            Log.d("HoleAdapter", "NULL Hole");
            return;
        }

//        val item: Hole = course!!.holes.get(position);
//        holder.holeButtonNumberTextView.text = item.hole_number.toString();

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
//        holder.button.setOnClickListener {
//
//        }
    }

}