package dev.coffee.examapp.model

data class WrongQuestion(
    val questionId: Int,
    val collected: Boolean,
    val content: String,
    val myAnswer: String,
    val correctAnswer: String
)
