package dev.coffee.examapp.ui.screens.exam

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.ui.theme.SuccessColor
import dev.coffee.examapp.viewmodel.ExamViewModel

@Composable
fun ExamScreen(
    examId: Int,
    duration: Int,
    questionIdStrings: String,
    onBack: () -> Unit
) {
    val questionIds = remember(questionIdStrings) {
        questionIdStrings.split(",").map { it.toInt() }
    }
    val context = LocalContext.current
    val viewModel: ExamViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExamViewModel(examId, duration, questionIds) as T
            }
        }
    )

    // 处理Toast显示
    val toastMessage by viewModel.showToast.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val examFinished by viewModel.examFinished.collectAsState()

    // 处理中途返回
    var showExitConfirmation by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        // 如果考试已经结束，直接返回
        if (examFinished) {
            onBack()
        } else {
            showExitConfirmation = true
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("退出考试") },
            text = { Text("中途退出将直接提交成绩，确定退出吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        viewModel.finishExam()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitConfirmation = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(text = "取消", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // 考试结束处理
    if (examFinished) {
        ExamResultScreen(scoreString = score.toString(), onBack = onBack)
        return
    }

    // 主界面
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // 顶部信息栏
        ExamHeader(
            remainingTime = remainingTime,
            currentIndex = currentIndex,
            totalQuestions = questionIds.size,
            score = score
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 题目内容区域
        QuestionContent(
            question = currentQuestion,
            isLoading = isLoading,
            onAnswerChanged = { viewModel.updateAnswer(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 导航按钮
        NavigationButtons(
            currentIndex = currentIndex,
            totalQuestions = questionIds.size,
            isLoading = isLoading,
            onPrevious = { viewModel.navigateToPrevious(context) },
            onNext = { viewModel.navigateToNext(context) },
            onSubmit = { viewModel.finishExam() }
        )
    }
}

@Composable
fun ExamHeader(
    remainingTime: Int,
    currentIndex: Int,
    totalQuestions: Int,
    score: Double
) {
    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 计时器
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "剩余时间",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                color = if (remainingTime < 60) Color(0xFFFF7043) else MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 进度
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "题目进度",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = "${currentIndex + 1}/$totalQuestions",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 当前得分
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "当前得分",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = "${score.toInt()}分",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuestionContent(
    question: Question?,
    isLoading: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 难度指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val difficultyText = when (question?.difficulty) {
                    1 -> "简单"
                    2 -> "中等"
                    3 -> "困难"
                    else -> ""
                }
                val difficultyColor = when (question?.difficulty) {
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
                // 题目内容
                Text(
                    text = question.content,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 答案输入区域
                Text(
                    text = "你的答案：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F51B5),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = question.myAnswer ?: "",
                    onValueChange = onAnswerChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("在此输入答案") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF3F51B5),
                        unfocusedIndicatorColor = Color(0xFFBDBDBD),
                    )
                )
            } else {
                Text(
                    text = "无法加载题目",
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NavigationButtons(
    currentIndex: Int,
    totalQuestions: Int,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPrevious,
            enabled = currentIndex > 0 && !isLoading,
            modifier = Modifier.width(120.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (isLoading && currentIndex > 0) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "上一题",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (currentIndex == totalQuestions - 1) {
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessColor.copy(0.4f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "提交",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Button(
                onClick = onNext,
                enabled = !isLoading,
                modifier = Modifier.width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "下一题",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
