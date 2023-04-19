package com.kylearon.fgcaddie.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class LocalCourseApiImpl(context: Context) : CourseApi{

    val context = context;

    //start with the empty course list
    var courses = Courses(ArrayList<Course>());

    init {

        //init the courses from static code here
//        val newCourse1 = Course(UUID.randomUUID().toString(), "Course 1", "creator", emptyList());
//        val newCourse2 = Course(UUID.randomUUID().toString(), "Course 2", "creator", emptyList());
//        val newCourse3 = Course(UUID.randomUUID().toString(), "Course 3", "creator", emptyList());
//
//        courses.courses.add(newCourse1);
//        courses.courses.add(newCourse2);
//        courses.courses.add(newCourse3);


        //init the courses from a saved file
        loadCourses();

    }

    override fun fetchCourses(): List<Course> {
        //TODO: instead make this re-fetch from the saved file and make a getCourses for local return?
        return courses.courses.toList();
    }

    override fun getCourse(courseId: String): Course? {
        val course: Course? = courses.courses.find { c -> c.guid.equals(courseId) }
        return course;
    }

    override fun addCourse(course: Course) {
        courses.courses.add(course);
        saveCourses();
    }

    override fun removeCourse(courseId: String) {
        courses.courses.removeIf { c -> c.guid.equals(courseId) }
        saveCourses();
    }

    override fun updateHole(hole: Hole) {
        Log.d("LocalCourseApiImpl", "updateHole()");

        val course: Course? = courses.courses.find { c -> c.guid.equals(hole.course_id) }
        if (course != null) {
            course.holes.forEach { h ->
                if(h.guid.equals(hole.guid)) {
                    h.par = hole.par;
                    h.length = hole.length;
                    h.shots_tee = hole.shots_tee;
                }
            }
        }

        Log.d("LocalCourseApiImpl", "HOLE JSON: " + Json.encodeToString(hole));

        saveCourses();
    }

    private fun loadCourses() {

        //read the courses file from local storage
        var file = File(context.filesDir, "courses.json");

        //create and re-lookup the file if it doesn't exist
        if(!file.exists()) {
            saveCourses();
            file = File(context.filesDir, "courses.json");
        }

        val fileReader = FileReader(file);
        val bufferedReader = BufferedReader(fileReader);

        val loadedText = bufferedReader.readText();
//        Log.d("LocalCourseApiImpl", loadedText);

        courses = Json.decodeFromString(loadedText);
    }

    private fun saveCourses() {
        val jsonString = Json.encodeToString(courses);
        Log.d("LocalCourseApiImpl", jsonString);

        //write the file out to local storage
        val file = File(context.filesDir, "courses.json");
        val fileWriter = FileWriter(file);
        val bufferedWriter = BufferedWriter(fileWriter);
        bufferedWriter.write(jsonString);
        bufferedWriter.close();

        Log.d("LocalCourseApiImpl", "Wrote to: " + file.absolutePath);
    }
}