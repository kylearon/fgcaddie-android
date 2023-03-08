package com.kylearon.fgcaddie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
