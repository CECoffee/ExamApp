package dev.coffee.examapp.model

data class Practice(
    val id: Int,
    val name: String,
    val questionCount: Int,
    val completedCount: Int,
    val progress: Double, // 0.0 to 1.0
    val chapters: List<Chapter>
)

data class Chapter(
    val id: Int,
    val name: String,
    val questionCount: Int,
    val completedCount: Int,
    val progress: Double // 0.0 to 1.0
)
