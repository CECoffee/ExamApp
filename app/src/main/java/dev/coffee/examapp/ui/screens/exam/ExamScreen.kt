package dev.coffee.examapp.ui.screens.exam

import dev.coffee.examapp.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import dev.coffee.examapp.model.ExamStatus
import dev.coffee.examapp.ui.components.ExamCard
import dev.coffee.examapp.ui.components.LoadingIndicator
import dev.coffee.examapp.viewmodel.ExamViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExamScreen(
    viewModel: ExamViewModel = viewModel()
) {
    // 模拟从ViewModel获取数据
    val exams by viewModel.exams.collectAsState()
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
            icon = Icons.Filled.CheckCircle,
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
        // TODO 传递真实的token
        viewModel.loadExams("fake_token")
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

                // Tab栏 - 使用TabRow实现居中
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

        // 加载状态
        if (isLoading) {
            LoadingIndicator()
        } else {
            // Tab内容
            HorizontalPager(state = pagerState) { page ->
                val filteredExams = viewModel.filterExamsByStatus(tabs[page].status)
                ExamList(exams = filteredExams, status = tabs[page].status)
            }
        }
    }
}

@Composable
fun ExamList(exams: List<dev.coffee.examapp.model.Exam>, status: ExamStatus) {
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
                    onStartExam = { /* 处理开始考试逻辑 */ },
                    onViewResult = { /* 处理查看成绩逻辑 */ }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector, val status: ExamStatus)