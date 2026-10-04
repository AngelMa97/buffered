package com.angelma.models

data class Video(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val master: String,
    val posterUrl: String,
    val backdropUrl: String,
    val license: License,
    val attribution: String,
    val renditions: List<Rendition>
)