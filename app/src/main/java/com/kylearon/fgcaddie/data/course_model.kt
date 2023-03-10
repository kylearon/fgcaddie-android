package com.kylearon.fgcaddie.data

import kotlinx.serialization.Serializable

@Serializable
data class FGCaddieModel(
    val courses: List<Course>
)


@Serializable
data class Courses(
    val courses: ArrayList<Course>
)

@Serializable
data class Course(
    val guid: String,
    val name: String,
    val creator: String,
    val holes: MutableList<Hole>
)

@Serializable
data class Hole(
    val guid: String,
    val course_id: String,
    val hole_number: Int,
    val par: Int,
    val length: Int,
    val shots_tee: MutableList<Shot>,
    val shots_approach: MutableList<Shot>,
    val shots_putt: MutableList<Shot>
)

@Serializable
data class Shot(
    val guid: String,
    val type: String,
    val distance: Int,
    val image_original: ByteArray,
    val image_markedup: ByteArray,
)
