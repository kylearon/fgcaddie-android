package com.kylearon.fgcaddie.coursebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kylearon.fgcaddie.R


class CourseBrowserFragment : Fragment() {

    var viewPager: ViewPager2? = null;
    var tabLayout: TabLayout? = null;
    var tabLayoutMediator: TabLayoutMediator? = null;

    private val tabFragments: ArrayList<Fragment> = arrayListOf(PublicCoursesFragment(), PrivateCoursesFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        //retrieve the Fragment arguments
        arguments?.let {

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_course_browser_page, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.browser_tab_layout);


        val viewPagerAdapter = ViewPagerAdapter(requireActivity(), tabFragments);
        viewPager!!.adapter = viewPagerAdapter;


        tabLayoutMediator = TabLayoutMediator(tabLayout!!, viewPager!!) { tab, position ->
            when (position) {
                0 -> tab.text = "Public"
                1 -> tab.text = "Private"
            }
        };
        tabLayoutMediator!!.attach();


        return view;
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onDestroy() {
        super.onDestroy();
        tabLayoutMediator?.detach();
        viewPager?.setAdapter(null);
    }

    companion object {
        private const val TAG = "CourseBrowserFragment"
    }
}