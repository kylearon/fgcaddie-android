package com.kylearon.fgcaddie.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.kylearon.fgcaddie.databinding.FragmentRulesPageBinding
import com.kylearon.fgcaddie.home.HomePageFragmentDirections

/**
 * Rules Page fragment.
 */
class RulesPageFragment : Fragment() {
    private var _binding: FragmentRulesPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentRulesPageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //set the click listener for the calendar button
        _binding!!.obstructionsButton.setOnClickListener {
            //create the action and navigate to the calendar fragment
            val action = RulesPageFragmentDirections.actionRulesPageFragmentToObstructionsPageFragment();
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
