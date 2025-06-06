package dev.coffee.examapp.model

data class Question(
    val id: Int,
    val difficulty: Int,
    val content: String,
    val questionType: QuestionType,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val myAnswer: String? = null,
    val explanation: String,
    val isCorrect: Boolean? = null
)

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, FILL_IN_THE_BLANK, SHORT_ANSWER
}