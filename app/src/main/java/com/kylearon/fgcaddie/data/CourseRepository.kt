package com.kylearon.fgcaddie.data

/**
 * https://developer.android.com/topic/architecture/data-layer
 */
class CourseRepository(
    private val courseRemoteDataSource: CourseRemoteDataSource
) {
    suspend fun fetchCourses(): List<Course> = courseRemoteDataSource.fetchCourses();

    fun getCourses(): List<Course> = courseRemoteDataSource.getCourses();

    fun getCourse(courseId: String) : Course? = courseRemoteDataSource.getCourse(courseId);

    fun addCourse(course: Course) = courseRemoteDataSource.addCourse(course);

    fun removeCourse(courseId: String) = courseRemoteDataSource.removeCourse(courseId);

    fun updateCourseName(courseId: String, name: String) = courseRemoteDataSource.updateCourseName(courseId, name);

    fun updateCourseColor(courseId: String, color: String) = courseRemoteDataSource.updateCourseColor(courseId, color);

    fun updateHole(hole: Hole) = courseRemoteDataSource.updateHole(hole);

    fun saveCourses() = courseRemoteDataSource.saveCourses();


}