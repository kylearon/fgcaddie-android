package com.kylearon.fgcaddie.hole

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.utils.FileUtils.Companion.getPrivateAppStorageFilepath
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HoleAdapter(private val holeFromParam: Hole) : RecyclerView.Adapter<HoleAdapter.HoleViewHolder>() {

    private lateinit var hole: Hole;

    private var parentView: View? = null;

    private var course: Course? = null;

    init {
        course = MainActivity.ServiceLocator.getCourseRepository().getCourse(holeFromParam.course_id);
        hole = course!!.holes.first { it.guid == holeFromParam.guid }
    }

    /**
     * Provides a reference for the views needed to display items in your list
     */
    class HoleViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val shotPhotoImageView = view.findViewById<ImageView>(R.id.shot_photo_image_view);
        val holePictureRowButtons = view.findViewById<LinearLayout>(R.id.hole_picture_row_buttons);
//        val reorderUpButton = view.findViewById<Button>(R.id.reorder_up_button);
//        val reorderDownButton = view.findViewById<Button>(R.id.reorder_down_button);
        val rotateImage = view.findViewById<ImageButton>(R.id.image_rotate);
        val shotNoteTextView = view.findViewById<TextView>(R.id.shot_note_text_view);
    }

    /**
     * Returns the number of items this view will show
     */
    override fun getItemCount(): Int {
        if(hole == null) {
            return 0;
        }

        return hole!!.shots_tee.size;
    }

    /**
     * Create a new view using the row_item_view template
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_shot_picture_row, parent, false);
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

        holder.shotPhotoImageView.visibility = View.VISIBLE;

        //get the shot
        val shot = hole.shots_tee[position];

        //construct the filepath and get the file
        val imageFilename = shot.image_markedup;
        val filepath = getPrivateAppStorageFilepath(imageFilename);

        Log.i("HoleAdapter", "filepath: " + filepath);

        //load the file into the ImageView using COIL
        holder.shotPhotoImageView.load(Uri.parse(filepath)) {
            scale(Scale.FILL)
        }

        //load the note for the shot
        holder.shotNoteTextView.text = shot.note;

        //click listener to show the full shot image
        holder.shotPhotoImageView.setOnClickListener {
            //create the action and navigate to the calendar fragment
            val action = HolePageFragmentDirections.actionHolePageFragmentToShotPageFragment(shot = Json.encodeToString(shot), hole = Json.encodeToString(hole));
            parentView!!.findNavController().navigate(action);
        }

        //listeners for the buttons
//        holder.reorderUpButton.setOnClickListener {
//            Log.i(TAG, "Reorder UP");
//        }
//
//        holder.reorderDownButton.setOnClickListener {
//            Log.i(TAG, "Reorder DOWN");
//        }

//        holder.rotateImage.setOnClickListener {
//            Log.i(TAG, "ROTATE IMAGE");
//
//            //load the bitmap
////            val loadedBitmap = BitmapFactory.decodeFile(Uri.parse(filepath).path);
//
//            //rotate it
//            holder.holePhotoImageView.rotation = holder.holePhotoImageView.rotation - 90f;
//
//        }
//
//        holder.holePhotoImageView.setOnLongClickListener {
//
//            // Your long press action goes here
//            holder.holePictureRowButtons.visibility = View.VISIBLE;
//
//            // Return true to indicate that you've handled the long press event
//            true
//        }
    }

    companion object {
        private const val TAG = "HoleAdapter"
    }

}