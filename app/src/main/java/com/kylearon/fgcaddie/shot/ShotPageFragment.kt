package com.kylearon.fgcaddie.shot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.kylearon.fgcaddie.data.Shot
import com.kylearon.fgcaddie.databinding.FragmentShotPageBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Shot Page fragment.
 */
class ShotPageFragment : Fragment() {
    private var _binding: FragmentShotPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var shot: Shot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the shot from the Fragment arguments
        arguments?.let {
            val shotJsonString = it.getString("shot");
            if (shotJsonString != null) {
                shot = Json.decodeFromString(shotJsonString);
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentShotPageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //construct the filepath and get the file
        val imageFilename = shot.image_markedup;
        val filepath = "/storage/emulated/0/Pictures/FGCaddie/" + imageFilename + ".png";

        _binding!!.shotImageView.load(File(filepath));
    }


    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}
