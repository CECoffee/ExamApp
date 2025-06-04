package dev.coffee.examapp.ui.screens.practice
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.viewmodel.PracticeViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import dev.coffee.examapp.model.Question

@Composable
fun PracticeScreen(
    chapterId: Int,
    chapterName: String,
    onBack: () -> Unit,
    viewModel: PracticeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PracticeViewModel(chapterId, chapterName) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswer by viewModel.userAnswer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val practiceFinished by viewModel.practiceFinished.collectAsState()
    val showExplanation by viewModel.showExplanation.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val showToast by viewModel.showToast.collectAsState()

    // Handle toast messages
    LaunchedEffect(showToast) {
        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle practice completion
    if (practiceFinished) {
        PracticeResultScreen(
            correctCount = correctCount,
            totalQuestions = viewModel.totalQuestions,
            onBack = onBack
        )
        return
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp)
    ) {
        // Header with progress and chapter name
        PracticeHeader(
            chapterName = chapterName,
            currentIndex = currentIndex,
            totalQuestions = viewModel.totalQuestions,
            correctCount = correctCount ,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question content
        QuestionContent(
            question = currentQuestion,
            isLoading = isLoading,
            userAnswer = userAnswer,
            onAnswerChanged = { viewModel.updateUserAnswer(it) },
            showExplanation = showExplanation,
            onSubmit = { viewModel.submitAnswer() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        if (showExplanation) {
            Button(
                onClick = { viewModel.proceedToNextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (currentIndex < viewModel.totalQuestions - 1) "下一题" else "完成练习",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            Button(
                onClick = { viewModel.submitAnswer() },
                enabled = userAnswer.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "提交答案",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeHeader(
    chapterName: String,
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
                    text = "<", // Back arrow symbol
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 8.dp)
                        .size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = chapterName,
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

@Composable
fun QuestionContent(
    question: Question?,
    isLoading: Boolean,
    userAnswer: String,
    onAnswerChanged: (String) -> Unit,
    showExplanation: Boolean,
    onSubmit: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (question != null) {
                // Difficulty indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val difficultyText = when (question.difficulty) {
                        1 -> "简单"
                        2 -> "中等"
                        3 -> "困难"
                        else -> ""
                    }
                    val difficultyColor = when (question.difficulty) {
                        1 -> Color(0xFF4CAF50)
                        2 -> Color(0xFFFFC107)
                        3 -> Color(0xFFF44336)
                        else -> Color.Gray
                    }

                    if (difficultyText.isNotEmpty()) {
                        Text(
                            text = difficultyText,
                            color = difficultyColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(4.dp)
                                .border(1.dp, difficultyColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question text
                Text(
                    text = question.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LatexWebView(
                    latex = question.content,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Answer input or explanation
                if (showExplanation) {
                    val resultColor = if (question.isCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val resultText = if (question.isCorrect == true) "回答正确" else "回答错误"

                    Text(
                        text = resultText,
                        color = resultColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "你的答案: $userAnswer",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (!question.isCorrect!!) {
                        Text(
                            text = "正确答案: ${question.correctAnswer}",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        text = "解析: ${question.explanation}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                } else {
                    TextField(
                        value = userAnswer,
                        onValueChange = onAnswerChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("在此输入你的答案") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        // [NEW] Added keyboard options for Enter key submission
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { onSubmit?.invoke() }
                        )
                    )
                }
            } else {
                Text(
                    text = "无法加载题目",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LatexWebView(latex: String, modifier: Modifier = Modifier) {
    val html = """
        <html>
        <head>
            <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
            <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
        </head>
        <body>
            <div id="math">${latex}</div>
            <script>
                MathJax.typesetPromise();
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
            }
        },
        update = { view ->
            view.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
    )
}
@Composable
fun PracticeResultScreen(
    correctCount: Int,
    totalQuestions: Int,
    onBack: () -> Unit
) {
    val percentage = (correctCount.toFloat() / totalQuestions) * 100
    val resultColor = when {
        percentage >= 80 -> Color(0xFF4CAF50)
        percentage >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    val resultText = when {
        percentage >= 80 -> "优秀！"
        percentage >= 60 -> "良好！"
        else -> "继续努力！"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "练习完成",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .border(8.dp, resultColor, CircleShape)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$correctCount/$totalQuestions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = resultColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = resultText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = resultColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "返回", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}