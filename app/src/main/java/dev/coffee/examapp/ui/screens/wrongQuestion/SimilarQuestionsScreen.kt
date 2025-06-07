package dev.coffee.examapp.ui.screens.wrongQuestion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.viewmodel.ExamViewModel

@Composable
fun SimilarQuestionsScreen(
    questionIdStrings: String,
    onBack: () -> Unit
) {
    val questionIds = remember(questionIdStrings) {
        questionIdStrings.split(",").map { it.toInt() }
    }
    // TODO
}