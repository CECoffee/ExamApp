package dev.coffee.examapp.model

data class Practice(
    val id: Int,
    val name: String,
    val questionCount: Int,
    val completedCount: Int,
    val chapters: List<Chapter>
)

data class Chapter(
    val id: String,
    val name: String,
    val questionCount: Int,
    val completedCount: Int
)
