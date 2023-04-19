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
    val name: String = "default name",
    val creator: String = "default creator",
    val date_created: String = "default date",
    val holes: MutableList<Hole> = ArrayList()
)

@Serializable
data class Hole(
    val guid: String,
    val course_id: String,
    val hole_number: Int,
    var par: Int,
    var length: Int,
    var shots_tee: MutableList<Shot> = ArrayList(),
    var shots_approach: MutableList<Shot> = ArrayList(),
    var shots_putt: MutableList<Shot> = ArrayList()
)

@Serializable
data class Shot(
    val guid: String,
    val type: String,
    val distance: Int,
    val image_original: String,
    val image_markedup: String,
)
