package com.kylearon.fgcaddie.messageboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kylearon.fgcaddie.databinding.FragmentMessageBoardPageBinding

/**
 * Message Board Page fragment.
 */
class MessageBoardPageFragment : Fragment() {
    private var _binding: FragmentMessageBoardPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentMessageBoardPageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }


    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}
