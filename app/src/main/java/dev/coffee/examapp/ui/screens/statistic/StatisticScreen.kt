package dev.coffee.examapp.ui.screens.statistic

import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.coffee.examapp.R
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.viewmodel.ExamListViewModel
import java.util.*
import kotlin.math.max

@Composable
fun StatisticScreen(
    navController: NavController,
    viewModel: ExamListViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val allExams by viewModel.exams.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val pageSize = 5
    val visibleItemCount = remember { mutableIntStateOf(pageSize) }

    val loadMore = { visibleItemCount.intValue += pageSize }

    val validExams = remember(allExams) {
        allExams
            .filter { it.status == ExamStatus.COMPLETED && it.score != null }
            .sortedBy { it.endTime }
    }

    LaunchedEffect(Unit) {
        if (allExams.isEmpty()) {
            viewModel.loadExams(ExamStatus.COMPLETED)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.ability_curve),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
            }
        } else if (validExams.isEmpty()) {
            EmptyStatisticsView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScoreTrendChart(
                    exams = validExams,
                    primaryColor = primaryColor,
                    surfaceColor = surfaceVariant,
                    textColor = onSurface
                )

                ExamRecordsSection(
                    exams = validExams,
                    visibleItemCount = visibleItemCount.intValue,
                    onLoadMore = loadMore
                )
            }
        }
    }
}

@Composable
private fun EmptyStatisticsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "暂无有效的考试成绩数据",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "完成考试后即可查看成绩趋势",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ScoreTrendChart(
    exams: List<Exam>,
    primaryColor: Color,
    surfaceColor: Color,
    textColor: Color
) {
    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.dp.toPx() }
            textAlign = Paint.Align.CENTER
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = 4.dp,
        backgroundColor = surfaceColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().background(surfaceColor).testTag("ScoreTrendCanvas")) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val padding = 40.dp.toPx()

                drawLine(
                    color = Color.Gray,
                    start = Offset(padding, canvasHeight - padding),
                    end = Offset(canvasWidth - padding, canvasHeight - padding),
                    strokeWidth = 2.dp.toPx()
                )

                drawLine(
                    color = Color.Gray,
                    start = Offset(padding, padding),
                    end = Offset(padding, canvasHeight - padding),
                    strokeWidth = 2.dp.toPx()
                )

                // 计算分数范围
                val maxScore = exams.maxOf { it.score ?: 0.0 }.toFloat() * 1.1f
                val minScore = max(0f, exams.minOf { it.score ?: 0.0 }.toFloat() * 0.9f)
                val scoreRange = maxScore - minScore
                val xStep = (canvasWidth - 2 * padding) / (exams.size - 1).coerceAtLeast(1)
                val yScale = (canvasHeight - 2 * padding) / scoreRange

                // 绘制网格线
                val ySteps = 5
                repeat(ySteps) { i ->
                    val y = canvasHeight - padding - (i * (canvasHeight - 2 * padding) / ySteps)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(padding, y),
                        end = Offset(canvasWidth - padding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 绘制数据点和曲线
                val path = Path().apply {
                    exams.forEachIndexed { index, exam ->
                        val x = padding + index * xStep
                        val y = canvasHeight - padding - ((exam.score?.toFloat() ?: 0f) - minScore) * yScale

                        if (index == 0) moveTo(x, y) else lineTo(x, y)

                        drawCircle(
                            color = primaryColor,
                            center = Offset(x, y),
                            radius = 5.dp.toPx()
                        )

                        // 绘制X轴标签
                        val dateLabel = SimpleDateFormat("MM/dd", Locale.getDefault())
                            .format(exam.endTime)
                        drawContext.canvas.nativeCanvas.drawText(
                            dateLabel, x, canvasHeight - padding / 2, textPaint
                        )

                        // 绘制分数标签
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(exam.score ?: 0.0), x, y - 10.dp.toPx(), textPaint
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun ExamRecordsSection(
    exams: List<Exam>,
    visibleItemCount: Int,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "考试记录 ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 400.dp)
        ) {
            items(exams.take(visibleItemCount)) { exam ->
                ExamScoreItem(exam = exam)
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            if (exams.size > visibleItemCount) {
                item {
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding( vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("加载更多...")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamScoreItem(exam: Exam) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = exam.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal
                )
            )
            Text(
                text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(exam.endTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Text(
            text = "%.1f分".format(exam.score ?: 0.0),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
