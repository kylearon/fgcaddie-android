package com.kylearon.fgcaddie.coursebrowser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kylearon.fgcaddie.R


class PrivateCoursesFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i("PrivateCoursesFragment", "PrivateCoursesFragment CREATED");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_private_courses, container, false);
    }

    companion object {

    }
}