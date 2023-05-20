package com.kylearon.fgcaddie.courseholes

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import com.kylearon.fgcaddie.databinding.FragmentCourseHolesPageBinding

class CourseHolesPageFragment: Fragment() {
    private var _binding: FragmentCourseHolesPageBinding? = null;

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!;

    private lateinit var recyclerView: RecyclerView;

    private lateinit var courseId: String
    
    private var course: Course? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the courseid from the Fragment arguments
        arguments?.let {
            courseId = it.getString("courseid").toString()
            course = MainActivity.ServiceLocator.getCourseRepository().getCourse(courseId);
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentCourseHolesPageBinding.inflate(inflater, container, false);
        val view = binding.root;
        return view;
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = binding.recyclerView;
        recyclerView.layoutManager = LinearLayoutManager(requireContext());
        recyclerView.adapter = CourseHolesAdapter(courseId);

        //set the name of the page
        (activity as? AppCompatActivity)?.supportActionBar?.title = course!!.name;

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner and an optional Lifecycle.State (here, RESUMED) to indicate when the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.rename_course_menu, menu)
                menuInflater.inflate(R.menu.share_course_menu, menu)
                menuInflater.inflate(R.menu.remove_course_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_remove_course -> {

                        //show a confirmation dialog
                        val confirmDeleteDialog = ConfirmDeleteDialogFragment(courseId, view);

                        //call add course
                        confirmDeleteDialog.show(childFragmentManager, ConfirmDeleteDialogFragment.TAG);

                        true
                    }
                    R.id.action_share_course -> {

                        //show a dialog
                        val shareCourseDialog = ShareCourseDialogFragment(courseId, view);

                        //call share course
                        shareCourseDialog.show(childFragmentManager, ShareCourseDialogFragment.TAG);

                        true
                    }
                    R.id.action_rename_course -> {

                        //show a dialog
                        val renameCourseDialog = RenameCourseDialogFragment(courseId);

                        //call rename course
                        renameCourseDialog.show(childFragmentManager, RenameCourseDialogFragment.TAG);

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
        super.onDestroyView()
        _binding = null
    }
}