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

    fun addCourse(course: Course) = courseApi.addCourse(course)

    fun removeCourse(course: Course) = courseApi.removeCourse(course)
}

// Makes course-related network synchronous requests.
interface CourseApi {
    fun fetchCourses(): List<Course>

    fun addCourse(course: Course)

    fun removeCourse(course: Course)
}
