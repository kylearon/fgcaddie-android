package com.kylearon.fgcaddie.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot
import com.kylearon.fgcaddie.databinding.FragmentCameraPageBinding
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Camera Page fragment.
 */
class CameraPageFragment : Fragment() {

    private var imageCaptureUseCase: ImageCapture? = null;
    private var originalBitmap: Bitmap? = null;


    private lateinit var cameraExecutor: ExecutorService;

    private lateinit var hole: Hole;

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

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentCameraPageBinding.inflate(inflater, container, false);
        val view = binding.root;

        // Set up the listeners for take photo and video capture buttons
        _binding!!.imageCaptureButton.setOnClickListener { takePhoto() }
        _binding!!.drawButton.setOnClickListener { drawOnImage() }
        _binding!!.saveButton.setOnClickListener { saveImage() }

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

        //re-enable the Take Photo functionality for this button if they clicked on it when it was Back
        if(_binding!!.imageCaptureButton.text.equals("Back"))
        {
            //change the buttons back to normal
            _binding!!.imageCaptureButton.text = "Take Photo";
            _binding!!.saveButton.visibility = View.GONE;

            //change the view back to normal
            _binding!!.viewFinder.visibility = View.VISIBLE;
            _binding!!.shotView.visibility = View.GONE;

            return;
        }

        //change the buttons to draw mode with a still image
        _binding!!.imageCaptureButton.text = "Back";
        _binding!!.saveButton.visibility = View.VISIBLE;

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
                    Log.d(TAG, msg);

                    //change the view
                    _binding!!.viewFinder.visibility = View.GONE;
                    _binding!!.shotView.visibility = View.VISIBLE;

                    //get the image bytes and turn them into a bitmapImage
                    val buffer = imageProxy.planes[0].buffer;
                    val bytes = ByteArray(buffer.capacity());
                    buffer[bytes];
                    val bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size);
                    imageProxy.close();

                    //scale the bitmap down so it doesn't take as much memory
                    val scaledWidth = (bitmapImage.width * .25).toInt();
                    val scaledHeight = (bitmapImage.height * .25).toInt();
                    val scaledBitmapImage = Bitmap.createScaledBitmap(bitmapImage, scaledWidth, scaledHeight, true);

                    //rotate the bitmap before it is put into the ImageView
                    val rotatedBitmapImage = rotateBitmap(scaledBitmapImage, imageProxy.imageInfo.rotationDegrees.toFloat());

                    //save this as the original image
                    originalBitmap = rotatedBitmapImage;

                    //send this to the DrawableCanvasView
                    _binding!!.shotView.setBackgroundImage(rotatedBitmapImage!!);
                }
            }
        )

    }

    private fun rotateBitmap(original: Bitmap, degrees: Float): Bitmap? {
        val width = original.width;
        val height = original.height;
        val matrix = Matrix();
        matrix.preRotate(degrees);
        val rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
        return rotatedBitmap;
    }

    private fun drawOnImage() {
        Log.d(TAG, "drawOnImage()");
    }

    private fun saveImage() {
        Log.d(TAG, "saveImage()");

        //show the saving text on screen
        _binding!!.savingTextView.visibility = View.VISIBLE;
        _binding!!.savingTextView.invalidate();

        //construct the image filename
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date());
        val filename: String = "hole-shot-" + hole.guid + "-" + timeStamp;

        //construct the Shot to add to the Hole model object
        hole.shots_tee.add(Shot(UUID.randomUUID().toString(), "all", 0, filename, filename ));


        // Create a new coroutine to move the execution off the UI thread
        GlobalScope.launch (Dispatchers.IO) {

            //save the bitmap image to private app storage
            _binding!!.shotView.saveBitmap(filename);

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
