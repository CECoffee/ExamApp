package dev.coffee.examapp.ui.screens.wrongQuestion

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.ui.components.QuestionCard
import dev.coffee.examapp.ui.screens.practice.PracticeResultScreen
import dev.coffee.examapp.viewmodel.SimilarQuestionsViewModel

@Composable
fun SimilarQuestionsScreen(
    questionIdStrings: String,
    onBack: () -> Unit
) {
    val questionIds = remember(questionIdStrings) {
        questionIdStrings.split(",").map { it.toInt() }
    }
    val context = LocalContext.current
    val viewModel: SimilarQuestionsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SimilarQuestionsViewModel(questionIds) as T
            }
        }
    )

    val toastMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userAnswer by viewModel.userAnswer.collectAsState()
    val practiceFinished by viewModel.practiceFinished.collectAsState()
    val showExplanation by viewModel.showExplanation.collectAsState()

    BackHandler(enabled = true) {
        onBack()
    }

    if (practiceFinished) {
        PracticeResultScreen(
            correctCount = correctCount,
            totalQuestions = questionIds.size,
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SimilarQuestionsHeader(
            currentIndex = currentIndex,
            totalQuestions = questionIds.size,
            correctCount = correctCount,
            onBack = onBack,
        )

        Spacer(modifier = Modifier.height(8.dp))

        QuestionCard(
            question = currentQuestion,
            isLoading = isLoading,
            onAnswerChanged = { viewModel.updateAnswer(it) },
            userAnswer = userAnswer,
            showExplanation = showExplanation
        )

        Spacer(modifier = Modifier.weight(1f))

        if (showExplanation) {
            Button(
                onClick = { viewModel.loadNextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) { Text(text = if (currentIndex < questionIds.size - 1) "下一题" else "完成") }
        } else {
            Button(
                onClick = { viewModel.submitCurrentAnswer() },
                enabled = userAnswer.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp) .testTag("Loading"))
                } else { Text(text = "提交答案") }
            }
        }
    }
}

@Composable
fun SimilarQuestionsHeader(
    currentIndex: Int,
    totalQuestions: Int,
    correctCount: Int,
    onBack: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "<",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 8.dp)
                        .size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "错题本",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("当前进度", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${currentIndex + 1}/$totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("正确数量", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "$correctCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("正确率", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${(correctCount.toFloat() / (currentIndex + 1) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}