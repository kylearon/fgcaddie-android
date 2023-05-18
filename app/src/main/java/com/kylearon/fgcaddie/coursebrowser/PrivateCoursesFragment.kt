package com.kylearon.fgcaddie.coursebrowser

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.databinding.FragmentPrivateCoursesBinding
import com.kylearon.fgcaddie.databinding.FragmentPublicCoursesBinding
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class PrivateCoursesFragment : Fragment() {

    private var _binding: FragmentPrivateCoursesBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var recyclerView: RecyclerView;
    private lateinit var privateCoursesAdapter: PrivateCoursesAdapter;

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

        // Retrieve and inflate the layout for this fragment
        _binding = FragmentPrivateCoursesBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = binding.recyclerView;

        recyclerView.layoutManager = LinearLayoutManager(requireContext());

        privateCoursesAdapter = PrivateCoursesAdapter(requireActivity());
        recyclerView.adapter = privateCoursesAdapter;

        updateCourses();
    }

    private fun updateCourses() {
        //send GET api/courses
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response: HttpResponse = MainActivity.ServiceLocator.getHttpClient().get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = MainActivity.StaticVals.RAILWAY_URL
                        path("api/courses")
                        parameters.append("api-key", "android")
                    }
                }


                Log.i(TAG, "Response status: ${response.status}")
                Log.i(TAG, "Response body: ${response.bodyAsText()}")

//                val courses = Courses(ArrayList<Course>());
                val courses = ArrayList<Course>();
                courses.addAll(Json.decodeFromString(response.bodyAsText()));

                //filter out the private courses into their own array
                val privateCourses = courses.filter { course -> course.password != null && course.password != "" }
                privateCoursesAdapter.updateData(privateCourses);

            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage)
            }
        }
    }

    companion object {
        private const val TAG = "PrivateCoursesFragment"
    }
}