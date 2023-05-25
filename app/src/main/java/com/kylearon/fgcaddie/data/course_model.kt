package com.kylearon.fgcaddie.data

import android.util.Log
import com.kylearon.fgcaddie.utils.FileUtils.Companion.getDatetimeReadable
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.collections.ArrayList

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
    var name: String = "default name",
    var creator: String = "default creator",
    val date_created: String = "default date",
    var password: String = "",  //only set the password before POSTing this object to the server to password-protect it
    var tag: String = "",
    var color: String = "",
    var image: String = "",
    val holes: MutableList<Hole> = ArrayList()
) {
    fun deepCopy(): Course {
        val newCourseGuid = UUID.randomUUID().toString()
        return Course(
            newCourseGuid,
            this.name,
            this.creator,
            getDatetimeReadable(),
            this.password,
            this.tag,
            this.color,
            this.image,
            this.holes.map { it.deepCopy(newCourseGuid) }.toMutableList()
        )
    }
}

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
) {
    fun deepCopy(newCourseId: String): Hole {
        return Hole(
            UUID.randomUUID().toString(),
            newCourseId,
            this.hole_number,
            this.par,
            this.length,
            this.shots_tee.map { it.deepCopy() }.toMutableList(),
            this.shots_approach.map { it.deepCopy() }.toMutableList(),
            this.shots_putt.map { it.deepCopy() }.toMutableList()
        )
    }

    /**
     * Only updates the note. In the future could update more parts of the Shot model
     */
    fun updateShot(shot: Shot) {
        val shotToUpdate = shots_tee.find { it.guid == shot.guid }

        if(shotToUpdate != null) {
            shotToUpdate!!.note = shot.note;
        } else {
            Log.i("Hole()", "ERROR no shot to update")
        }
    }
}

@Serializable
data class Shot(
    val guid: String,
    val type: String,
    val distance: Int,
    var note: String = "",
    var image_original: String,
    var image_markedup: String,
) {
    fun deepCopy(): Shot {
        return Shot(
            UUID.randomUUID().toString(),
            this.type,
            this.distance,
            this.note,
            this.image_original,
            this.image_markedup
        )
    }
}
