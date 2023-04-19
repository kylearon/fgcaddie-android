package com.kylearon.fgcaddie.data

import kotlinx.serialization.Serializable


@Serializable
data class Settings(
    val user_name: String
)