package com.kylearon.fgcaddie.hole

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kylearon.fgcaddie.HomePageFragmentDirections
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Hole
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class HoleAdapter(hole: Hole) : RecyclerView.Adapter<HoleAdapter.HoleViewHolder>() {

    private var hole: Hole = hole;

    private var parentView: View? = null;

    init {
//        GlobalScope.launch {
//            hole =
//        }
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class HoleViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val holePhotoImageView = view.findViewById<ImageView>(R.id.hole_photo_image_view);
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

        if(hole == null) {
            return 0;
        }

        return hole!!.shots_tee.size;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_hole_picture_view, parent, false);
        parentView = parent;
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

        Log.d("HoleAdapter", "Showing Hole Photos");

        holder.holePhotoImageView.visibility = View.VISIBLE;

        //get the shot
        val shot = hole.shots_tee[position];

        //construct the filepath and get the file
        val imageFilename = shot.image_markedup;
//        val filepath = "file:///storage/emulated/0/Pictures/FGCaddie/" + imageFilename + ".png";
        val filepath = "file:///data/user/0/com.kylearon.fgcaddie/files/" + imageFilename + ".png";
//        val loadedFile = File(filepath);

//        Log.d("HoleAdapter", "loading photo: " + filepath);
//        Log.d("HoleAdapter", "image Exists: " + loadedFile.exists().toString());

        //load the file into the ImageView using COIL
        holder.holePhotoImageView.load(Uri.parse(filepath));

        //click listener to show the full shot image
        holder.holePhotoImageView.setOnClickListener {
            //create the action and navigate to the calendar fragment
            val action = HolePageFragmentDirections.actionHolePageFragmentToShotPageFragment(shot = Json.encodeToString(shot), hole = Json.encodeToString(hole));
            parentView!!.findNavController().navigate(action);
        }

//        holder.holePhotoImaveView.setImageBitmap(Json.decodeFromString(hole.shots_tee[position].image_markedup));


//        val item: Hole = course!!.holes.get(position);
//        holder.holeButtonNumberTextView.text = item.hole_number.toString();

        // Assigns a [OnClickListener] to the button contained in the [ViewHolder]
//        holder.button.setOnClickListener {
//
//        }
    }

}