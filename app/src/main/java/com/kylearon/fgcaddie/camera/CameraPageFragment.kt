package com.kylearon.fgcaddie.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.kylearon.fgcaddie.databinding.FragmentCameraPageBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera Page fragment.
 */
class CameraPageFragment : Fragment() {

    private var imageCaptureUseCase: ImageCapture? = null;

    private lateinit var cameraExecutor: ExecutorService;

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentCameraPageBinding.inflate(inflater, container, false);
        val view = binding.root;

        // Set up the listeners for take photo and video capture buttons
        _binding!!.imageCaptureButton.setOnClickListener { takePhoto() }
        _binding!!.videoCaptureButton.setOnClickListener {  }
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
            //save the photo
            val finalBitmap = _binding!!.shotView.toBitmap();

            //change the buttons back to normal
            _binding!!.imageCaptureButton.text = "Take Photo";
            _binding!!.videoCaptureButton.visibility = View.VISIBLE;
            _binding!!.saveButton.visibility = View.GONE;

            //change the view back to normal
            _binding!!.viewFinder.visibility = View.VISIBLE;
            _binding!!.shotView.visibility = View.GONE;

            return;
        }

        //change the buttons to draw mode with a still image
        _binding!!.imageCaptureButton.text = "Back";
        _binding!!.videoCaptureButton.visibility = View.GONE;
        _binding!!.saveButton.visibility = View.VISIBLE;


        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCaptureUseCase ?: return;

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
            }
        }

        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(requireActivity().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc);
                    _binding!!.imageCaptureButton.text = "Take Photo";
                }

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
//                    val msg = "Photo capture succeeded: ${image.}";

                    val msg = "PHOTO CALLBACK " + imageProxy.width + " " + imageProxy.height;
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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

                    Log.d(TAG, "bitmap size: " + bitmapImage.width + " " + bitmapImage.height);

                    //rotate the bitmap before it is put into the ImageView
                    val rotatedBitmapImage = rotateBitmap(bitmapImage, imageProxy.imageInfo.rotationDegrees.toFloat());

                    //send this to the DVC
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
        _binding!!.shotView.saveBitmap();
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
