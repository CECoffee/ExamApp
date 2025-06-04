package dev.coffee.examapp.ui.screens.exam

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.coffee.examapp.R
import dev.coffee.examapp.model.Exam
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.components.ExamCard
import dev.coffee.examapp.ui.components.LoadingIndicator
import dev.coffee.examapp.ui.navigation.Screen
import dev.coffee.examapp.viewmodel.ExamListViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExamListScreen(
    navController: NavController,
    viewModel: ExamListViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val tabs = listOf(
        TabItem(
            title = stringResource(R.string.tab_pending),
            icon = Icons.Filled.Schedule,
            status = ExamStatus.PENDING
        ),
        TabItem(
            title = stringResource(R.string.tab_completed),
            icon = Icons.Outlined.CheckCircle,
            status = ExamStatus.COMPLETED
        ),
        TabItem(
            title = stringResource(R.string.tab_expired),
            icon = Icons.Filled.History,
            status = ExamStatus.EXPIRED
        )
    )

    val pagerState = rememberPagerState { tabs.size }

    // 初始化加载数据
    LaunchedEffect(Unit) {
        viewModel.loadExams()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 标题
                Text(
                    text = stringResource(R.string.exam_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Tab栏
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        /* 测试用 */
        val testExams = listOf(
            Exam(
                id = 1,
                name = "数学考试",
                startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time, // 明天开始
                endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.time,   // 后天结束
                duration = 90,
                status = ExamStatus.PENDING,
                questionList = listOf(1,2,3)
            ),
            Exam(
                id = 2,
                name = "历史考试",
                startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -10) }.time, // 10天前开始
                endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -5) }.time,    // 5天前结束
                duration = 60,
                status = ExamStatus.COMPLETED,
                score = 85.0,
                questionList = listOf(2,4,5),
            ),
            Exam(
                id = 3,
                name = "物理考试",
                startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -20) }.time, // 20天前开始
                endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -15) }.time,   // 15天前结束
                duration = 120,
                status = ExamStatus.EXPIRED,
                questionList = listOf(1,3,4)
            )
        )


        // 加载状态
        if (isLoading) {
            LoadingIndicator()
        } else {
            // Tab内容
            HorizontalPager(state = pagerState) { page ->
                val filteredExams = viewModel.filterExamsByStatus(tabs[page].status)
                // val filteredExams = testExams.filter { it.status == tabs[page].status }  // 测试用
                ExamList(exams = filteredExams, status = tabs[page].status, navController = navController)
            }
        }
    }
}

@Composable
fun ExamList(exams: List<Exam>, status: ExamStatus, navController: NavController) {
    if (exams.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (status) {
                    ExamStatus.PENDING -> "没有待考考试"
                    ExamStatus.COMPLETED -> "没有已完成的考试"
                    ExamStatus.EXPIRED -> "没有已过期的考试"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exams.forEach { exam ->
                ExamCard(
                    exam = exam,
                    onStartExam = { navController.navigate(Screen.Exam.route + "/${exam.id}/${exam.duration}/${exam.questionList.joinToString(",")}") },
                    onViewResult = { navController.navigate(Screen.ExamResult.route + "/${exam.score}") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector, val status: ExamStatus)