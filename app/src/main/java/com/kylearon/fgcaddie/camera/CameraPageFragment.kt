package com.kylearon.fgcaddie.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.slider.Slider
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.MainActivity.StaticVals.HD_IMAGE_HEIGHT
import com.kylearon.fgcaddie.MainActivity.StaticVals.HD_IMAGE_WIDTH
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot
import com.kylearon.fgcaddie.databinding.FragmentCameraPageBinding
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.utils.BitmapUtils.Companion.rotateBitmap
import com.kylearon.fgcaddie.utils.FileUtils.Companion.SHOT_TYPE_MARKEDUP
import com.kylearon.fgcaddie.utils.FileUtils.Companion.SHOT_TYPE_ORIGINAL
import com.kylearon.fgcaddie.utils.FileUtils.Companion.constructImageFilename


/**
 * Camera Page fragment.
 */
class CameraPageFragment : Fragment(), EditNoteDialogFragment.EditNoteDialogListener {

    private var imageCaptureUseCase: ImageCapture? = null;
    private var originalBitmap: Bitmap? = null;
    private lateinit var prevSelectedImageButton: ImageButton;

    private lateinit var cameraExecutor: ExecutorService;

    private lateinit var hole: Hole;

    private var shot: Shot? = null;

    private var _binding: FragmentCameraPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //retrieve the courseid from the Fragment arguments
        arguments?.let {
            val holeString = it.getString("hole").toString();
            hole = Json.decodeFromString(holeString);
        }

        //create a Shot object to fill the data into. the Shot will be saved to the Hole when saveImage() is called
        initShot();

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentCameraPageBinding.inflate(inflater, container, false);
        val view = binding.root;

        //setup the listeners for take photo and video capture buttons
        _binding!!.imageCaptureButton.setOnClickListener { takePhoto() }
        _binding!!.saveButton.setOnClickListener { saveImage() }
        _binding!!.retakeButton.setOnClickListener { retakeImage() }

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

        //setup the listener for the edit text
        _binding!!.editText.setOnClickListener { editTextForShot() }

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
                _binding!!.shotView.setPencilThickness(slider.value);
            }
        })

        //start the camera thread
        cameraExecutor = Executors.newSingleThreadExecutor();

        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    private fun startCamera() {
        //Create an instance of the ProcessCameraProvider. This is used to bind the lifecycle of cameras to the lifecycle owner.
        //This eliminates the task of opening and closing the camera since CameraX is lifecycle-aware
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        //Add a listener to the cameraProviderFuture with a Runnable as one argument.
        //Add ContextCompat.getMainExecutor() as the second argument. This returns an Executor that runs on the main thread.
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get();

            // build the preview and add it to the @viewFinder android element
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(_binding!!.viewFinder.surfaceProvider);
                }

            //set the imageCapture when the camera is started so it can be used to take pictures
            imageCaptureUseCase = ImageCapture.Builder().build();


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, imageCaptureUseCase);

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc);
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private fun takePhoto() {

        //hide the take photo button
        _binding!!.imageCaptureButton.visibility = View.GONE;


        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCaptureUseCase ?: return;

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc);
                    _binding!!.imageCaptureButton.text = "Take Photo";
                }

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val msg = "PHOTO CALLBACK " + imageProxy.width + " " + imageProxy.height;
//                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, msg);

                    //change the view to show the recorded image
                    _binding!!.viewFinder.visibility = View.GONE;
                    _binding!!.shotView.visibility = View.VISIBLE;

                    //get the image bytes and turn them into a bitmapImage
                    val buffer = imageProxy.planes[0].buffer;
                    val bytes = ByteArray(buffer.capacity());
                    buffer[bytes];
                    val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size);
                    imageProxy.close();

                    //scale the bitmap down so it doesn't take as much memory
                    //
                    //the photo is taken sideways, so we need to rotate it.
                    //but it is more efficient to scale it down before rotating, so swap the width/height target values for scaling
                    val PRE_ROTATED_WIDTH = HD_IMAGE_HEIGHT;
                    val PRE_ROTATED_HEIGHT = HD_IMAGE_WIDTH;

                    //get the proposed scalar values
                    val widthScalarToHD = PRE_ROTATED_WIDTH.toDouble() / bitmapImage.width;
                    val heightScalarToHD = PRE_ROTATED_HEIGHT.toDouble() / bitmapImage.height;

                    //use the lower value, and scale both axes of the photo by it to maintain original ratio
                    val overallScalarToHD: Double = if (widthScalarToHD < heightScalarToHD) widthScalarToHD else heightScalarToHD;
                    val scaledWidth = (overallScalarToHD * bitmapImage.width).toInt();
                    val scaledHeight = (overallScalarToHD * bitmapImage.height).toInt();

//                    Log.i(TAG, "Scaled Width: " + scaledWidth);
//                    Log.i(TAG, "Scaled Height: " + scaledHeight);

                    //TODO: test this with rotated devices
                    val scaledBitmapImage = Bitmap.createScaledBitmap(bitmapImage, scaledWidth, scaledHeight, true);

                    //rotate the bitmap before it is put into the ImageView
                    val rotatedBitmapImage = rotateBitmap(scaledBitmapImage, imageProxy.imageInfo.rotationDegrees.toFloat());

                    //save this as the original image
                    originalBitmap = rotatedBitmapImage;

                    //show the palette for editing the shot
                    _binding!!.cameraButtonsLayout.visibility = View.VISIBLE;

                    //send this to the DrawableCanvasView
                    _binding!!.shotView.setBackgroundImage(rotatedBitmapImage!!);
                }
            }
        )

    }

    private fun initShot() {

        //get a guid for this new shot
        val shotGuid = UUID.randomUUID().toString();

        //construct the image filenames
        val filenameOriginal = constructImageFilename(shotGuid, SHOT_TYPE_ORIGINAL);
        val filenameMarkedup = constructImageFilename(shotGuid, SHOT_TYPE_MARKEDUP);

        //construct the Shot to use
        shot = Shot(shotGuid, "all", 0, "", filenameOriginal, filenameMarkedup );
    }


    private fun saveImage() {
        Log.d(TAG, "saveImage()");

        //show the saving text on screen
        _binding!!.savingTextView.visibility = View.VISIBLE;
        _binding!!.savingTextView.invalidate();

        //this should ideally never be true here
        if(shot == null) {
            initShot();
        }

        //add the Shot which was initialized in onCreate() to the Hole
        hole.shots_tee.add(shot!!);

        // Create a new coroutine to move the execution off the UI thread
        GlobalScope.launch (Dispatchers.IO) {

            //save the markedup and original bitmap images to private app storage
            _binding!!.shotView.saveCurrentBitmap(shot!!.image_markedup);
            _binding!!.shotView.saveOriginalBitmap(shot!!.image_original);

            //save the Hole json to the local model
            Log.d(TAG, "saveBitmapToModel()");
            MainActivity.ServiceLocator.getCourseRepository().updateHole(hole); //TODO: add hole as fragment param to this class

        }.invokeOnCompletion {

            //navigate back in the Main thread once the bitmap is done being saved to internal storage
            runBlocking {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "NAVIGATE BACK");
                    //then navigate back once the image is saved to internal storage
                    val action = CameraPageFragmentDirections.actionCameraPageFragmentToHolePageFragment(hole = Json.encodeToString(hole));
                    _binding!!.root.findNavController().navigate(action);
                }
            }
        }

    }

    //setup the ui so you can retake the image
    private fun retakeImage() {
        //change the buttons back to normal
        _binding!!.imageCaptureButton.visibility = View.VISIBLE;
        _binding!!.cameraButtonsLayout.visibility = View.GONE;

        //change the view back to normal
        _binding!!.viewFinder.visibility = View.VISIBLE;
        _binding!!.shotView.visibility = View.GONE;

        return;
    }


    private fun pencilUndo() {
        _binding!!.shotView.undoDraw();
    }

    private fun pencilRedo() {
        _binding!!.shotView.redoDraw();
    }

    private fun pencilClearDrawings() {
        _binding!!.shotView.clearDrawings();
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
        _binding!!.shotView.setPencilColor(color);
    }


    private fun showPencilTypeDialog() {

        //toggle the pencil slider visibility
        if(_binding!!.pencilThicknessLayout.visibility == View.GONE) {
            _binding!!.pencilThicknessLayout.visibility = View.VISIBLE;
        } else {
            _binding!!.pencilThicknessLayout.visibility = View.GONE;
        }

    }

    private fun editTextForShot() {
        //show the edit note dialog?
        val editNoteDialog = EditNoteDialogFragment(hole, shot!!, this);
        editNoteDialog.show(childFragmentManager, EditNoteDialogFragment.TAG);
    }

    override fun onEditNoteSaved() {
        setNoteText(shot!!.note);
    }

    fun setNoteText(note: String) {
        _binding!!.shotNoteTextView.text = note;
        _binding!!.shotNoteTextView.visibility = View.VISIBLE;
    }

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(),"Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        }
    }

    companion object {
        private const val TAG = "CameraPageFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


}
