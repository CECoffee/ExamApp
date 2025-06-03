package dev.coffee.examapp.model

import java.util.*

data class Exam(
    val id: String,
    val title: String,
    val subject: String,
    val startTime: Date,
    val endTime: Date,
    val duration: Int, // 分钟
    val status: ExamStatus,
    val score: Int? = null,
    val totalQuestions: Int,
    val passed: Boolean? = null
) {
    val formattedTime: String
        get() = "${startTime.toString().substring(0, 10)} - ${endTime.toString().substring(0, 10)}"
}

enum class ExamStatus {
    PENDING, COMPLETED, EXPIRED
}