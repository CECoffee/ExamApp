package dev.coffee.examapp.model

import java.util.*

data class Exam(
    val id: Int,
    val name: String,
    val startTime: Date,
    val endTime: Date,
    val duration: Int, // 秒
    val status: ExamStatus,
    val score: Int? = null,
    val questionList: List<Int>,
) {
    val formattedTime: String
        get() = "${startTime.toString().substring(0, 10)} - ${endTime.toString().substring(0, 10)}"
}

enum class ExamStatus {
    PENDING, COMPLETED, EXPIRED
}