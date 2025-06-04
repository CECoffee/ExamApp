package dev.coffee.examapp.model

data class Question(
    val id: Int,
    val difficulty: Int,
    val content: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val correctAnswer: String,
    val myAnswer: String? = null,
    val explanation: String? = null,
    val isCorrect: Boolean
)

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, FILL_IN_THE_BLANK
}