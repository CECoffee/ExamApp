package dev.coffee.examapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.theme.ErrorColor
import dev.coffee.examapp.ui.theme.InfoColor
import dev.coffee.examapp.ui.theme.SuccessColor
import dev.coffee.examapp.ui.theme.WarningColor

@Composable
fun ExamCard(
    exam: Exam,
    onStartExam: () -> Unit = {},
    onViewResult: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val statusTriple: Triple<String, Color, ImageVector> = when (exam.status) {
                    ExamStatus.PENDING -> Triple<String, Color, ImageVector>("待考", InfoColor, Icons.Filled.DateRange)
                    ExamStatus.COMPLETED -> Triple("已考", SuccessColor, Icons.Filled.CheckCircle)
                    ExamStatus.EXPIRED -> Triple("已过期", WarningColor, Icons.Filled.Close)
                }
                val statusText = statusTriple.first
                val statusColor = statusTriple.second
                val icon = statusTriple.third

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = statusText,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 考试信息
            ExamInfoItem(label = "科目：", value = exam.subject)
            ExamInfoItem(label = "时间：", value = exam.formattedTime)
            ExamInfoItem(label = "时长：", value = "${exam.duration}分钟")

            if (exam.status == ExamStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(8.dp))
                ExamInfoItem(
                    label = "成绩：",
                    value = "${exam.score}/${exam.totalQuestions}",
                    valueColor = if (exam.passed == true) SuccessColor else ErrorColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            when (exam.status) {
                ExamStatus.PENDING -> {
                    Button(
                        onClick = onStartExam,  // 修复：直接传递函数引用
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("开始考试")
                    }
                }
                ExamStatus.COMPLETED -> {
                    OutlinedButton(
                        onClick = onViewResult,  // 修复：直接传递函数引用
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("查看成绩")
                    }
                }
                ExamStatus.EXPIRED -> {
                    // 过期考试没有操作按钮
                }
            }
        }
    }
}

@Composable
fun ExamInfoItem(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}