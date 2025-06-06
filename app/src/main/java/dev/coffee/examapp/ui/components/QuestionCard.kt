package dev.coffee.examapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.coffee.examapp.model.Question

@Composable
fun QuestionCard(
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

                LatexWebview(
                    latex = question.content,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .background(Color.Transparent)
                )

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

                    Text(
                        text = "正确答案: ${question.correctAnswer}",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "解析:",
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    question.correctAnswer?.let {
                        LatexWebview(
                            latex = it,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .background(Color.Transparent)
                        )
                    }

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