package com.kylearon.fgcaddie.home

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.databinding.FragmentHomePageBinding

/**
 * Entry fragment for the app. Displays the Home Page.
 */
class HomePageFragment : Fragment() {
    private var _binding: FragmentHomePageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentHomePageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner and an optional Lifecycle.State (here, RESUMED) to indicate when the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.settings_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_settings -> {

                        val settingsDialog = SettingsDialogFragment();
                        settingsDialog.show(childFragmentManager, SettingsDialogFragment.TAG);

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //set the click listener for the course notes button
        _binding!!.courseNotesButton.setOnClickListener {
            //create the action and navigate to the course notes fragment
            val action = HomePageFragmentDirections.actionHomePageFragmentToCourseNotesPageFragment();
            view.findNavController().navigate(action);
        }

        //set the click listener for the calendar button
        _binding!!.calendarButton.setOnClickListener {
            //create the action and navigate to the calendar fragment
            val action = HomePageFragmentDirections.actionHomePageFragmentToCalendarPageFragment();
            view.findNavController().navigate(action);
        }
    }

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}
