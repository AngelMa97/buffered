package com.angelma.models

data class Video(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val master: String,
    val poster: String,
    val backdrop: String,
    val license: License,
    val attribution: String,
    val renditions: List<Rendition>
)