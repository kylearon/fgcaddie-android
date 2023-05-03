package com.kylearon.fgcaddie.camera

import android.graphics.*
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

    private var _binding: FragmentDrawImagePageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the courseid from the Fragment arguments
        arguments?.let {
            val holeString = it.getString("hole").toString();
            hole = Json.decodeFromString(holeString);
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentDrawImagePageBinding.inflate(inflater, container, false);
        val view = binding.root;

        //setup the listeners for take photo and video capture buttons
//        _binding!!.imageCaptureButton.setOnClickListener { takePhoto() }
        _binding!!.saveButton.setOnClickListener { saveImage() }
        _binding!!.retakeButton.setOnClickListener { retakeImage() }

        //setup the listeners for undo/redo
        _binding!!.pencilUndo.setOnClickListener { pencilUndo() }
        _binding!!.pencilRedo.setOnClickListener { pencilRedo() }

        //setup the listeners for the pencil colors
        _binding!!.pencilColor1.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor2.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor3.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor4.setOnClickListener { setPencilColor(it) }
        _binding!!.pencilColor5.setOnClickListener { setPencilColor(it) }

        //select a button to start
        prevSelectedImageButton = _binding!!.pencilColor3;

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


        //fill out the background with a white canvas
        val bitmapImage = Bitmap.createBitmap(HD_IMAGE_WIDTH, HD_IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapImage)
        canvas.drawColor(Color.WHITE)
        _binding!!.drawableImageView.setBackgroundImage(bitmapImage!!);

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

//    private fun startCamera() {
//        //Create an instance of the ProcessCameraProvider. This is used to bind the lifecycle of cameras to the lifecycle owner.
//        //This eliminates the task of opening and closing the camera since CameraX is lifecycle-aware
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
//
//        //Add a listener to the cameraProviderFuture with a Runnable as one argument.
//        //Add ContextCompat.getMainExecutor() as the second argument. This returns an Executor that runs on the main thread.
//        cameraProviderFuture.addListener({
//            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get();
//
//            // build the preview and add it to the @viewFinder android element
//            val previewUseCase = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(_binding!!.viewFinder.surfaceProvider);
//                }
//
//            //set the imageCapture when the camera is started so it can be used to take pictures
//            imageCaptureUseCase = ImageCapture.Builder().build();
//
//
//            // Select back camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll();
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, imageCaptureUseCase);
//
//            } catch(exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc);
//            }
//
//        }, ContextCompat.getMainExecutor(requireContext()));
//    }

//    private fun takePhoto() {
//
//        //hide the take photo button
//        _binding!!.imageCaptureButton.visibility = View.GONE;
//
//        //show the palette for editing the shot
//        _binding!!.cameraButtonsLayout.visibility = View.VISIBLE;
//
//        // Get a stable reference of the modifiable image capture use case
//        val imageCapture = imageCaptureUseCase ?: return;
//
//        imageCapture.takePicture(
//            ContextCompat.getMainExecutor(requireContext()),
//            object : ImageCapture.OnImageCapturedCallback() {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc);
//                    _binding!!.imageCaptureButton.text = "Take Photo";
//                }
//
//                override fun onCaptureSuccess(imageProxy: ImageProxy) {
//                    val msg = "PHOTO CALLBACK " + imageProxy.width + " " + imageProxy.height;
////                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, msg);
//
//                    //change the view to show the recorded image
//                    _binding!!.viewFinder.visibility = View.GONE;
//                    _binding!!.shotView.visibility = View.VISIBLE;
//
//                    //get the image bytes and turn them into a bitmapImage
//                    val buffer = imageProxy.planes[0].buffer;
//                    val bytes = ByteArray(buffer.capacity());
//                    buffer[bytes];
//                    val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size);
//                    imageProxy.close();
//
//                    //scale the bitmap down so it doesn't take as much memory
//                    val scaledWidth = (bitmapImage.width * .25).toInt();
//                    val scaledHeight = (bitmapImage.height * .25).toInt();
//                    val scaledBitmapImage = Bitmap.createScaledBitmap(bitmapImage, scaledWidth, scaledHeight, true);
//
//                    //rotate the bitmap before it is put into the ImageView
//                    val rotatedBitmapImage = rotateBitmap(scaledBitmapImage, imageProxy.imageInfo.rotationDegrees.toFloat());
//
//                    //save this as the original image
//                    originalBitmap = rotatedBitmapImage;
//
//                    //send this to the DrawableCanvasView
//                    _binding!!.shotView.setBackgroundImage(rotatedBitmapImage!!);
//                }
//            }
//        )
//
//    }

//    private fun rotateBitmap(original: Bitmap, degrees: Float): Bitmap? {
//        val width = original.width;
//        val height = original.height;
//        val matrix = Matrix();
//        matrix.preRotate(degrees);
//        val rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
//        return rotatedBitmap;
//    }


    private fun saveImage() {
        Log.d(TAG, "saveImage()");

        //show the saving text on screen
        _binding!!.savingTextView.visibility = View.VISIBLE;
        _binding!!.savingTextView.invalidate();

        //construct the image filename
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date());
        val filename: String = "hole-shot-" + hole.guid + "-" + timeStamp + ".png";

        //construct the Shot to add to the Hole model object
        hole.shots_tee.add(Shot(UUID.randomUUID().toString(), "all", 0, filename, filename ));


        // Create a new coroutine to move the execution off the UI thread
        GlobalScope.launch (Dispatchers.IO) {

            //save the bitmap image to private app storage
            _binding!!.drawableImageView.saveBitmap(filename);

            //save the Hole json to the local model
            Log.d(TAG, "saveBitmapToModel()");
            MainActivity.ServiceLocator.getCourseRepository().updateHole(hole); //TODO: add hole as fragment param to this class

        }.invokeOnCompletion {

            //navigate back in the Main thread once the bitmap is done being saved to internal storage
            runBlocking {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "NAVIGATE BACK");
                    //then navigate back once the image is saved to internal storage
                    val action = DrawImagePageFragmentDirections.actionDrawImagePageFragmentToHolePageFragment(hole = Json.encodeToString(hole));
                    _binding!!.root.findNavController().navigate(action);
                }
            }
        }

    }

    //setup the ui so you can retake the image
    private fun retakeImage() {
        //change the buttons back to normal
//        _binding!!.imageCaptureButton.visibility = View.VISIBLE;
//        _binding!!.cameraButtonsLayout.visibility = View.GONE;

        //change the view back to normal
//        _binding!!.viewFinder.visibility = View.VISIBLE;
//        _binding!!.shotView.visibility = View.GONE;

        return;
    }


    private fun pencilUndo() {
        _binding!!.drawableImageView.undoDraw();
    }


    private fun pencilRedo() {
        _binding!!.drawableImageView.redoDraw();
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