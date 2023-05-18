package com.kylearon.fgcaddie.coursebrowser

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.kylearon.fgcaddie.MainActivity
import com.kylearon.fgcaddie.R
import com.kylearon.fgcaddie.data.Course
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class PrivateCoursesAdapter(fragmentActivity: FragmentActivity) : PublicCoursesAdapter(fragmentActivity) {

    override fun onBindViewHolder(holder: PublicCoursesViewHolder, position: Int) {
        super.onBindViewHolder(holder, position);

        holder.downloadClickable.setOnClickListener{
            Log.i(TAG, "Clicked DOWNLOAD private course");
        }
    }


    companion object {
        const val TAG = "PrivateCoursesAdapter"
    }

}