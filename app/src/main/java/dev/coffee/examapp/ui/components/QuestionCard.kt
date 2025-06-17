package dev.coffee.examapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.coffee.examapp.model.Question
import dev.coffee.examapp.model.QuestionType
import dev.coffee.examapp.model.QuestionType.*
import dev.coffee.examapp.ui.components.latex.LatexWebview
import dev.coffee.examapp.ui.components.latex.MathLiveEditor
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun QuestionCard(
    question: Question?,
    isLoading: Boolean,
    userAnswer: String,
    onAnswerChanged: (String) -> Unit,
    showExplanation: Boolean
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

                Box {
                    LatexWebview(
                        latex = question.content,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .background(Color.Transparent)
                            .testTag("question_content_tag"),
                        onClick = {}
                    )
                }

                val optionLabels = listOf("A", "B", "C", "D", "E", "F", "G", "H")
                if (showExplanation) {
                    val resultColor = if (question.correctAnswer == userAnswer) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val resultText = if (question.correctAnswer == userAnswer) "回答正确" else "回答错误"

                    Text(
                        text = resultText,
                        color = resultColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (question.questionType == FILL_IN_THE_BLANK) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "你的答案:", modifier = Modifier.wrapContentWidth())
                            LatexWebview(
                                latex = userAnswer,
                                Modifier.fillMaxWidth().testTag("user_answer_tag"),
                                onClick = {}
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "正确答案:", modifier = Modifier.padding(bottom = 8.dp))
                            LatexWebview(
                                latex = question.correctAnswer ?: "",
                                modifier = Modifier.fillMaxWidth().testTag("correct_answer_tag"),
                                onClick = {}
                            )
                        }
                    } else {
                        val userSelectedIndices = userAnswer.trim('[', ']')
                            .split(",").mapNotNull { it.toIntOrNull() }.toSet()

                        val correctIndices = question.correctAnswer
                            ?.trim('[', ']')
                            ?.split(",")
                            ?.mapNotNull { it.toIntOrNull() }
                            ?.toSet() ?: emptySet()

                        question.options?.forEachIndexed { index, option ->
                            val label = optionLabels.getOrNull(index) ?: index.toString()
                            val isUserSelected = index in userSelectedIndices
                            val isCorrectAnswer = index in correctIndices

                            val bgColor = when {
                                isCorrectAnswer && isUserSelected -> Color(0xFF4CAF50) // 正确
                                isCorrectAnswer && !isUserSelected -> Color(0xFFFFC107) // 漏选
                                !isCorrectAnswer && isUserSelected -> Color(0xFFF44336) // 错选
                                else -> Color.LightGray // 其它未选
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LatexWebview(
                                    latex = option,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .weight(1f),
                                    onClick = {}
                                )
                            }
                        }
                    }

                    Text(
                        text = "解析:",
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    LatexWebview(
                        latex = question.explanation,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .background(Color.Transparent)
                            .testTag("explanation_content_tag"),
                        onClick = {}
                    )
                } else {
                    when (question.questionType) {

                        FILL_IN_THE_BLANK -> {
                            MathLiveEditor(
                                initialLatex = userAnswer,
                                onAnswerChanged = { newLatex ->
                                    val finalLatex = newLatex.replace("""\exponentialE""", "e")
                                    onAnswerChanged(finalLatex)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("fill_blank_input")
                                    .wrapContentHeight()
                            )
                        }

                        SINGLE_CHOICE -> {
                            val selectedIndex = userAnswer.trim('[', ']').toIntOrNull()
                            Column {
                                question.options?.forEachIndexed { index, option ->
                                    val label = optionLabels.getOrNull(index) ?: index.toString()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onAnswerChanged("[$index]") }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (selectedIndex == index) MaterialTheme.colorScheme.primary else Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                        LatexWebview(
                                            latex = option,
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .weight(1f),
                                            onClick = { onAnswerChanged("[$index]") }
                                        )
                                    }
                                }
                            }
                        }

                        MULTIPLE_CHOICE -> {
                            val selectedIndices = remember(userAnswer) {
                                userAnswer.trim('[', ']')
                                    .split(",")
                                    .mapNotNull { it.toIntOrNull() }
                                    .toMutableSet()
                            }

                            Column {
                                question.options?.forEachIndexed { index, option ->
                                    val isSelected = index in selectedIndices
                                    val label = optionLabels.getOrNull(index) ?: index.toString()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isSelected) selectedIndices.remove(index) else selectedIndices.add(index)
                                                onAnswerChanged("[" + selectedIndices.sorted().joinToString(",") + "]")
                                            }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RectangleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                        LatexWebview(
                                            latex = option,
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .weight(1f),
                                            onClick = {
                                                if (isSelected) selectedIndices.remove(index) else selectedIndices.add(index)
                                                onAnswerChanged("[" + selectedIndices.sorted().joinToString(",") + "]")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        TRUE_FALSE -> {
                            val options = listOf("对", "错")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                options.forEach { text ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { onAnswerChanged(text) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (userAnswer == text) MaterialTheme.colorScheme.primary else Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
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