package com.kylearon.fgcaddie.data

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class CourseRepository(
    private val courseRemoteDataSource: CourseRemoteDataSource
) {
    suspend fun fetchCourses(): List<Course> = courseRemoteDataSource.fetchCourses()

    fun addCourse(course: Course) = courseRemoteDataSource.addCourse(course)

    fun removeCourse(course: Course) = courseRemoteDataSource.removeCourse(course)
}