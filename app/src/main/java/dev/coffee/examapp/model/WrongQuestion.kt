package dev.coffee.examapp.model

import java.util.*

data class WrongQuestion(
    val id: String,
    val examId: String,
    val examTitle: String,
    val subject: String,
    val content: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val userAnswer: String,
    val correctAnswer: String,
    val collected: Boolean,
    val lastSeen: Date,
    val explanation: String? = null
)

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
}