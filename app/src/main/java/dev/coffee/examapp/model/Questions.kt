package dev.coffee.examapp.model

data class Questions (
    val id: Int,
    val scoreValue: Double,
    val difficulty: Int,
    val content: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val correctAnswer: String,
    val myAnswer: String? = null,
    val explanation: String? = null
)

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
}