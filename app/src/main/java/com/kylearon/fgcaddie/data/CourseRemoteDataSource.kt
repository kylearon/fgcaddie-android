package com.kylearon.fgcaddie.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class CourseRemoteDataSource(
    private val courseApi: CourseApi,
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Fetches the courses from the network and returns the result.
     * This executes on an IO-optimized thread pool, the function is main-safe.
     */
    suspend fun fetchCourses(): List<Course> =
        // Move the execution to an IO-optimized thread since the ApiService doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            courseApi.fetchCourses()
        }

    fun getCourses() = courseApi.getCourses()

    fun getCourse(courseId: String) = courseApi.getCourse(courseId)

    fun addCourse(course: Course) = courseApi.addCourse(course)

    fun removeCourse(courseId: String) = courseApi.removeCourse(courseId)

    fun updateCourseName(courseId: String, name: String) = courseApi.updateCourseName(courseId, name)

    fun updateCourseColor(courseId: String, color: String) = courseApi.updateCourseColor(courseId, color)

    fun updateHole(hole: Hole) = courseApi.updateHole(hole)

    fun saveCourses() = courseApi.saveCourses()
}

// Makes course-related network synchronous requests.
interface CourseApi {
    fun fetchCourses(): List<Course>

    fun getCourses(): List<Course>

    fun getCourse(courseId: String): Course?

    fun addCourse(course: Course)

    fun removeCourse(courseId: String)

    fun updateCourseName(courseId:String, name: String)

    fun updateCourseColor(courseId:String, color: String)

    fun updateHole(hole: Hole)

    fun saveCourses()
}
