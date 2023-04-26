package com.kylearon.fgcaddie.shot

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import coil.load
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.courseholes.ConfirmDeleteDialogFragment
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.data.Shot
import com.kylearon.fgcaddie.databinding.FragmentShotPageBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Shot Page fragment.
 */
class ShotPageFragment : Fragment() {
    private var _binding: FragmentShotPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var shot: Shot
    private lateinit var hole: Hole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the shot from the Fragment arguments
        arguments?.let {
            val shotJsonString = it.getString("shot");
            if (shotJsonString != null) {
                shot = Json.decodeFromString(shotJsonString);
            }

            val holeJsonString = it.getString("hole");
            if (holeJsonString != null) {
                hole = Json.decodeFromString(holeJsonString);
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
        val filepath = "file:///data/user/0/com.kylearon.fgcaddie/files/" + imageFilename;

        //load the image into the ImageView using COIL
        _binding!!.shotImageView.load(Uri.parse(filepath));


        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner and an optional Lifecycle.State (here, RESUMED) to indicate when the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.remove_shot_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_remove_shot -> {

                        //show a confirmation dialog
                        val confirmDeleteDialog = ConfirmDeleteShotDialogFragment(shot.guid, hole, view);
                        confirmDeleteDialog.show(childFragmentManager, ConfirmDeleteDialogFragment.TAG);

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }


    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    companion object {
        const val TAG = "ShotPageFragment"
    }

}
