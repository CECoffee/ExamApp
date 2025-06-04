package dev.coffee.examapp.model

data class WrongQuestion(
    val questionId: Int,
    val content: String,
    val myAnswer: String,
    val correctAnswer: String
)
