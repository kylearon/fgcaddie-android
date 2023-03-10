package com.kylearon.fgcaddie.data

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class CourseRepository(
    private val courseRemoteDataSource: CourseRemoteDataSource
) {
    suspend fun fetchCourses(): List<Course> = courseRemoteDataSource.fetchCourses();

    fun getCourse(courseId: String) : Course? = courseRemoteDataSource.getCourse(courseId);

    fun addCourse(course: Course) = courseRemoteDataSource.addCourse(course);

    fun removeCourse(courseId: String) = courseRemoteDataSource.removeCourse(courseId);
}