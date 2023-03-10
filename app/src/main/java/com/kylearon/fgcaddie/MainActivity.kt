package com.kylearon.fgcaddie

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.kylearon.fgcaddie.data.CourseRemoteDataSource
import com.kylearon.fgcaddie.data.CourseRepository
import com.kylearon.fgcaddie.data.LocalCourseApiImpl
import com.kylearon.fgcaddie.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers

/**
 * Main Activity and entry point for the app.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    object ServiceLocator {

        private val courseRemoteDataSource: CourseRemoteDataSource = CourseRemoteDataSource(LocalCourseApiImpl(myApplication.baseContext), Dispatchers.IO);
        private val courseRepository: CourseRepository = CourseRepository(courseRemoteDataSource);
        fun getCourseRepository(): CourseRepository = courseRepository;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        myApplication = application;

        val binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        // Get the navigation host fragment from this Activity
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment;

        // Instantiate the navController using the NavHostFragment
        navController = navHostFragment.navController;

        // Make sure actions in the ActionBar get propagated to the NavController
        setupActionBarWithNavController(navController);
    }

    /**
     * Enables back button support. Simply navigates one element up on the stack.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    companion object {

        //make the application and thus the context available statically in the app
        //https://stackoverflow.com/a/54110003
        private lateinit var myApplication: Application

    }
}
