package com.kylearon.fgcaddie

import android.app.Application
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.kylearon.fgcaddie.data.*
import com.kylearon.fgcaddie.databinding.ActivityMainBinding
import io.ktor.client.*
import io.ktor.client.plugins.*
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

        private val settingsRemoteDataSource: SettingsRemoteDataSource = SettingsRemoteDataSource(LocalSettingsApiImpl(myApplication.baseContext));
        private val settingsRepository: SettingsRepository = SettingsRepository(settingsRemoteDataSource);
        fun getSettingsRepository(): SettingsRepository = settingsRepository;


        private val httpClient = HttpClient() {
            install(HttpTimeout) {
                requestTimeoutMillis = 60000
                connectTimeoutMillis = 60000
                socketTimeoutMillis  = 60000
            }
        }
        fun getHttpClient() : HttpClient = httpClient;
    }

    object StaticVals {
        val AWS_URL: String = "https://fgcaddie.s3.us-east-2.amazonaws.com";
        val RAILWAY_URL: String = "expressjs-postgres-production-3edc.up.railway.app";
        val ANDROID_BASE_FILEPATH = "file:///data/user/0/com.kylearon.fgcaddie/files/";

        //there are in portrait orientation
        val HD_IMAGE_WIDTH = 720;
        val HD_IMAGE_HEIGHT = 1280;
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (navController.currentDestination?.id != R.id.homePageFragment) {
            menuInflater.inflate(R.menu.home_button_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home_button -> {
                navController.navigate(R.id.action_global_homePageFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
