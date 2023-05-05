package com.kylearon.fgcaddie.camera

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.slider.Slider
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.databinding.FragmentDrawImagePageBinding


/**
 * Draw Image Page fragment.
 */
class DrawImagePageFragment : Fragment() {

    private val HD_IMAGE_WIDTH = 720;
    private val HD_IMAGE_HEIGHT = 1280;

    private lateinit var prevSelectedImageButton: ImageButton;


    private lateinit var hole: Hole;
    private var shot: Shot? = null;

    private var _binding: FragmentDrawImagePageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var backgroundImage: Bitmap;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the courseid from the Fragment arguments
        arguments?.let {
            val holeString = it.getString("hole").toString();
            hole = Json.decodeFromString(holeString);


            //get the shot if it exists. this is the way to edit a shot rather than add a new shot to a hole
            val shotJsonString: String? = it.getString("shot");
            if (shotJsonString != null && shotJsonString.isNotEmpty()) {
                //parse the shot json
                shot = Json.decodeFromString(shotJsonString);

                //construct the filepath and get the bitmap from the file
                val imageFilename = shot!!.image_markedup;
                val filepath = MainActivity.StaticVals.ANDROID_BASE_FILEPATH + imageFilename;

                //get the background image to edit from the shot
                Log.i(TAG, "Loading image to edit: " + Uri.parse(filepath).path);
                val loadedBitmap = BitmapFactory.decodeFile(Uri.parse(filepath).path);

                //make the loaded bitmap mutable so it can be used in the canvas
                backgroundImage = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
            else
            {
                //fill out the background with a white bitmap if a shot wasn't passed in to edit
                backgroundImage = Bitmap.createBitmap(HD_IMAGE_WIDTH, HD_IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
                val canvas = Canvas(backgroundImage);
                canvas.drawColor(Color.WHITE);
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentDrawImagePageBinding.inflate(inflater, container, false);
        val view = binding.root;

        //setup the listener for save button
        _binding!!.saveButton.setOnClickListener { saveImage() }

        //setup the listeners for undo/redo and clear
        _binding!!.pencilUndo.setOnClickListener { pencilUndo() }
        _binding!!.pencilRedo.setOnClickListener { pencilRedo() }
        _binding!!.pencilClearDrawings.setOnClickListener { pencilClearDrawings() }

        //setup the listeners for the pencil colors
        _binding!!.pencilColor1.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor2.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor3.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor4.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor5.setOnClickListener { setPencilColor(it) }

        //select a button to start
        prevSelectedImageButton = _binding!!.pencilColor3;

        //setup listener for showing the thickness slider
        _binding!!.pencilTypeButton.setOnClickListener { showPencilTypeDialog() }

        //setup listeners for the thickness slider on change
        _binding!!.pencilThicknessSlider.addOnChangeListener { slider, value, fromUser ->
            // Handle slider value change
            Log.d("Slider", "Value: $value")

            _binding!!.pencilThicknessSliderValue.text = value.toInt().toString();
        }

        //setup listeners for the thickness slider finished sliding
        _binding!!.pencilThicknessSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //do something when the user starts sliding
            }
            override fun onStopTrackingTouch(slider: Slider) {
                //set the pencil thickness when the user finishes sliding
                _binding!!.drawableImageView.setPencilThickness(slider.value);
            }
        })

        //draw the background bitmap into the View
        _binding!!.drawableImageView.setBackgroundImage(backgroundImage!!);

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }


    private fun saveImage() {
        Log.d(TAG, "saveImage()");

        //show the saving text on screen
        _binding!!.savingTextView.visibility = View.VISIBLE;
        _binding!!.savingTextView.invalidate();

        if(shot != null) {
            //since the Shot already exists on the Hole object, we just need to save the bitmap
            //we do not need to update the CourseRepository Hole storage like we do below for a new Shot drawing
            val filename = shot!!.image_markedup;

            // Create a new coroutine to move the execution off the UI thread
            GlobalScope.launch(Dispatchers.IO) {
                //save the bitmap image to private app storage
                _binding!!.drawableImageView.saveBitmap(filename);
            }.invokeOnCompletion {
                //navigate back in the Main thread once the bitmap is done being saved to internal storage
                runBlocking {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "NAVIGATE BACK TO SHOT");
                        //then navigate back once the image is saved to internal storage
                        val action = DrawImagePageFragmentDirections.actionDrawImagePageFragmentToShotPageFragment(hole = Json.encodeToString(hole), shot = Json.encodeToString(shot));
                        _binding!!.root.findNavController().navigate(action);
                    }
                }
            }

        }
        else {
            //construct the image filename
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date());
            val filename: String = "hole-shot-" + hole.guid + "-" + timeStamp + ".png";

            //construct the Shot to add to the Hole model object
            hole.shots_tee.add(Shot(UUID.randomUUID().toString(), "all", 0, filename, filename));

            // Create a new coroutine to move the execution off the UI thread
            GlobalScope.launch(Dispatchers.IO) {

                //save the bitmap image to private app storage
                _binding!!.drawableImageView.saveBitmap(filename);

                //save the Hole json to the local model
                Log.d(TAG, "saveBitmapToModel()");
                MainActivity.ServiceLocator.getCourseRepository().updateHole(hole);

            }.invokeOnCompletion {

                //navigate back in the Main thread once the bitmap is done being saved to internal storage
                runBlocking {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "NAVIGATE BACK");
                        //then navigate back once the image is saved to internal storage
                        val action =
                            DrawImagePageFragmentDirections.actionDrawImagePageFragmentToHolePageFragment(
                                hole = Json.encodeToString(hole)
                            );
                        _binding!!.root.findNavController().navigate(action);
                    }
                }
            }
        }

    }


    private fun pencilUndo() {
        _binding!!.drawableImageView.undoDraw();
    }

    private fun pencilRedo() {
        _binding!!.drawableImageView.redoDraw();
    }

    private fun pencilClearDrawings() {
        _binding!!.drawableImageView.clearDrawings();
    }

    private fun setPencilColor(it: View) {

        //show this image button as selected, reset the prev selected one, save this as prev
        val imageButton: ImageButton = it as ImageButton;
        imageButton.setImageResource(R.drawable.baseline_radio_button_checked_24);
        prevSelectedImageButton.setImageResource(R.drawable.baseline_circle_24);
        prevSelectedImageButton = imageButton;

        //get the color from the ImageButton's tag and set it in the DrawableCanvas
        val colorResourceString = it.tag as String;
        val color = Color.parseColor(colorResourceString);
        _binding!!.drawableImageView.setPencilColor(color);
    }


    private fun showPencilTypeDialog() {

        //toggle the pencil slider visibility
        if(_binding!!.pencilThicknessLayout.visibility == View.GONE) {
            _binding!!.pencilThicknessLayout.visibility = View.VISIBLE;
        } else {
            _binding!!.pencilThicknessLayout.visibility = View.GONE;
        }

    }


    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    companion object {
        private const val TAG = "DrawImagePageFragment"
    }

}
