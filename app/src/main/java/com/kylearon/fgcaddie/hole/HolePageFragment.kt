package com.kylearon.fgcaddie.hole

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.data.Hole
import com.kylearon.fgcaddie.databinding.FragmentHolePageBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HolePageFragment: Fragment() {
    private var _binding: FragmentHolePageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var recyclerView: RecyclerView;

    private lateinit var hole: Hole

    private var course: Course? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the hole from the Fragment arguments
        arguments?.let {

            val holeJsonString = it.getString("hole");
            if(holeJsonString != null) {
                hole = Json.decodeFromString(holeJsonString);
                course = MainActivity.ServiceLocator.getCourseRepository().getCourse(hole.course_id);
            }

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentHolePageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = binding.recyclerView;
        recyclerView.layoutManager = LinearLayoutManager(requireContext());
        recyclerView.adapter = HoleAdapter(hole);

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity();

        //set the name of the page
//        val title = "Hole " + hole!!.hole_number.toString() + " Par " + hole!!.par + " " + hole!!.length + " yds";
        (activity as? AppCompatActivity)?.supportActionBar?.title =  "Hole " + hole!!.hole_number.toString();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner and an optional Lifecycle.State (here, RESUMED) to indicate when the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.edit_hole_menu, menu)
                menuInflater.inflate(R.menu.draw_picture_menu, menu)
                menuInflater.inflate(R.menu.take_picture_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_edit_hole -> {

                        //show a dialog with controls to edit the hole
                        val editCourseDialog = EditHoleDialogFragment(hole, recyclerView, binding.root);
                        editCourseDialog.show(childFragmentManager, EditHoleDialogFragment.TAG);

                        true
                    }
                    R.id.action_draw_picture -> {

                        //create the action and navigate to the draw image page fragment
                        val action = HolePageFragmentDirections.actionHolePageFragmentToDrawImagePageFragment(hole = Json.encodeToString(hole), shot = "");
                        view.findNavController().navigate(action);

                        true
                    }
                    R.id.action_take_picture -> {

                        //create the action and navigate to the camera page fragment
                        val action = HolePageFragmentDirections.actionHolePageFragmentToCameraPageFragment(hole = Json.encodeToString(hole));
                        view.findNavController().navigate(action);

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val holeTextString = "Hole " + hole.hole_number;
        binding.holeNumberText.text = holeTextString;

        val holeParString = "Par " + hole.par;
        binding.holeParText.text = holeParString;

        val holeDistanceString = hole.length.toString() + " yds";
        binding.holeDistanceText.text = holeDistanceString;

        binding.prevButton.setOnClickListener {
            //only scroll to prev hole if not at the start
            if(hole.hole_number - 2 >= 0) {
                val nextHoleIndex = hole.hole_number - 2;
                val nextHole = course!!.holes[nextHoleIndex];

                Log.i(TAG, "going back to hole index " + nextHoleIndex);

                val action = HolePageFragmentDirections.actionHolePageFragmentSelf(hole = Json.encodeToString(nextHole))
                it.findNavController().navigate(action);
            }
        }

        binding.nextButton.setOnClickListener {
            //only scroll to next hole if not at the end
            if(hole.hole_number < course!!.holes.size) {

                val nextHoleIndex = hole.hole_number;
                val nextHole = course!!.holes[nextHoleIndex];

                Log.i(TAG, "going forward to hole index " + nextHoleIndex);

                val action = HolePageFragmentDirections.actionHolePageFragmentSelf(hole = Json.encodeToString(nextHole))
                it.findNavController().navigate(action);
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Refresh or invalidate your data here

        Log.i(TAG, "onResume()");
        this.recyclerView.invalidate();
    }

    fun setHole(holeToSet: Hole) {
        hole = holeToSet;
    }

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HolePageFragment"
    }

}